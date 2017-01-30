import Build_gradle.Declaration.Companion.ENUM_TYPE
import org.gradle.api.GradleException
import java.io.File
import java.nio.charset.Charset

task("build") {
    val source = project.properties["apiFile"] ?: throw GradleException("Invalid 'apiFile' parameter value!")
    val file = file(source) ?: throw GradleException("No file located in '$source'")
    generateKotlinWrappers(file)
}

fun generateKotlinWrappers(sourceFile: File) {
    val lines = sourceFile.readLines(Charset.forName("UTF-8")).iterator()
    val declarations = mutableListOf<Declaration>()
    while (lines.hasNext()) {
        val declaration = DeclarationReader.read(lines)
        declarations.add(declaration)
    }

    val fileGenerator = FileGenerator(declarations)
    fileGenerator.generate(projectDir.resolve("generated/src/main/kotlin"))
}

object DeclarationReader {
    fun read(lineIterator: Iterator<String>): Declaration {
        if (lineIterator.next() != "/**") {
            throw IllegalStateException("Invalid comment start!")
        }

        val lines = mutableListOf<String>()
        while (true) {
            val line = lineIterator.next()
            if (line == " */") {
                return Declaration.parse(lineIterator.next(), lines)
            }

            lines.add(line.substring(3))
        }
    }
}

open class Declaration(val data: Data) {
    companion object {

        // TODO: move to script parameter
        val OBJECT_TYPE = "yfiles.lang.Object"
        val ENUM_TYPE = "yfiles.lang.Enum"

        val NAMESPACE = "@namespace"
        val CLASS = "@class "
        val INTERFACE = "@interface"
        val CONSTRUCTOR = "@constructor"

        val CONST = "@const"
        val STATIC = "@static"
        val FINAL = "@final"
        val ABSTRACT = "@abstract"
        val PROTECTED = "@protected"

        val IMPLEMENTS = "@implements "
        val EXTENDS = "@extends "
        val EXTENDS_ENUM = "${EXTENDS}{${ENUM_TYPE}}"
        val TEMPLATE = "@template "

        val PARAM = "@param "
        val RETURNS = "@returns "
        val TYPE = "@type "

        val GETS_OR_SETS = "Gets or sets"
        val NULL_VALUE = "null;"

        val FUNCTION_START = "function("
        val FUNCTION_END = "): "

        val GENERIC_START = ".<"
        val GENERIC_END = ">"

        val STANDARD_TYPE_MAP = mapOf(
                "Object" to OBJECT_TYPE,
                "object" to OBJECT_TYPE,
                "boolean" to "Boolean",
                "string" to "String",
                "number" to "Number"
        )

        fun parse(source: String, lines: List<String>): Declaration {
            val data = Data.parse(source)

            if (lines.contains(NAMESPACE)) {
                return Namespace(data)
            }

            if (lines.any { it.startsWith(CLASS) } && !lines.contains(CONSTRUCTOR)) {
                return ClassDec(data, lines)
            }

            if (lines.contains(INTERFACE)) {
                return InterfaceDec(data, lines)
            }

            if (lines.contains(EXTENDS_ENUM)) {
                return EnumDec(data, lines)
            }

            if (lines.contains(CONSTRUCTOR)) {
                return Constructor(data, lines)
            }

            if (lines.contains(CONST)) {
                return Const(data, lines)
            }

            if (!source.contains("=")) {
                return EnumValue(data, lines)
            }

            if (data.nullValue) {
                return Property(data, lines)
            }

            return Function(data, lines)
        }

        fun parseTypeLine(line: String): String {
            val i1 = line.indexOf("{")
            val i2 = line.indexOf("}")

            val type = line.substring(i1 + 1, i2)
            if (type.startsWith(FUNCTION_START)) {
                return parseFunctionType(type)
            }
            return parseType(type)
        }

        fun parseType(type: String): String {
            val standardType = STANDARD_TYPE_MAP[type]
            if (standardType != null) {
                return standardType
            }

            if (!type.contains(GENERIC_START)) {
                return type
            }

            return parseGenericType(type)
        }

        fun parseGenericType(type: String): String {
            val mainType = StringUtil.till(type, GENERIC_START)
            val parametrizedTypes = StringUtil.between(type, GENERIC_START, GENERIC_END).split(",").map { parseType(it) }
            return "$mainType<${parametrizedTypes.joinToString(", ")}>"
        }

        fun parseFunctionType(type: String): String {
            val parameterTypes = StringUtil.between(type, FUNCTION_START, FUNCTION_END).split(",").map({ parseType(it) })
            val resultType = parseType(StringUtil.from(type, FUNCTION_END))
            return "(${parameterTypes.joinToString(", ")}) -> $resultType"
        }
    }
}

class Data(val source: String, val name: String, val value: String) {
    companion object {
        fun parse(source: String): Data {
            val items = source.split("=")
            if (items.size == 1) {
                val name = source.substring(0, source.length - 1)
                return Data(source, name, Declaration.NULL_VALUE)
            }

            if (items.size != 2) {
                throw GradleException("Invalid declaration: '$source'")
            }

            return Data(source, items[0], items[1])
        }
    }

    val nullValue: Boolean
        get() = value == Declaration.NULL_VALUE
}

open class InstanceDec(data: Data, protected val lines: List<String>) : Declaration(data) {
    fun genericParameters(): List<GenericParameter> {
        val templateLine = lines.firstOrNull { it.startsWith(TEMPLATE) }
                ?: return emptyList()
        val names = StringUtil.from(templateLine, TEMPLATE).split(",")
        // TODO: support generic type read
        return names.map { GenericParameter(it, "") }
    }
}

class ClassDec(data: Data, lines: List<String>) : InstanceDec(data, lines) {
    val static = lines.contains(STATIC)
    val open = !lines.contains(FINAL)
    val abstract = lines.contains(ABSTRACT)
}

class InterfaceDec(data: Data, lines: List<String>) : InstanceDec(data, lines) {

}

class EnumDec(data: Data, lines: List<String>) : InstanceDec(data, lines) {

}

class Constructor(data: Data, private val lines: List<String>) : Declaration(data) {
    fun toClassDec(): ClassDec {
        return ClassDec(data, lines)
    }
}

class Const(data: Data, lines: List<String>) : Declaration(data) {
    val static = lines.contains(STATIC)
    val type: String
    val name: String
    val className: String

    init {
        type = parseTypeLine(lines.first { it.startsWith(TYPE) })

        val names = data.name.split(".")
        name = names.last()

        var i = names.size - 2
        if (names[i] == "prototype") {
            i--
        }
        className = names.subList(0, i + 1).joinToString(separator = ".")
    }
}

class Property(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Function(data: Data, private val lines: List<String>) : Declaration(data) {
    companion object {
        val START = "function("
        val END = "){};"
    }

    val parameters: List<Parameter>

    init {
        val value = data.value
        val parameterNames = StringUtil.hardBetween(value, START, END).split(",")
        parameters = parameterNames.map { Parameter(it, "") }
    }
}

class EnumValue(data: Data, private val lines: List<String>) : Declaration(data) {
    val className: String
    val name: String

    init {
        val dataName = data.name
        val index = dataName.lastIndexOf(".")
        className = dataName.substring(0, index)
        name = dataName.substring(index + 1, dataName.length)
    }
}

data class Parameter(val name: String, val type: String)
data class GenericParameter(val name: String, val type: String)

class Namespace(data: Data) : Declaration(data)

class Undefined(data: Data) : Declaration(data)

class FileGenerator(declarations: List<Declaration>) {

    private val classFileList: Set<ClassFile>
    private val interfaceFileList: Set<InterfaceFile>
    private val enumFileList: Set<EnumFile>

    init {
        val classFileList = mutableListOf<ClassFile>()
        classFileList.addAll(
                declarations
                        .filterIsInstance(ClassDec::class.java)
                        .map({ ClassFile(it) })
        )

        declarations.filterIsInstance(Constructor::class.java).forEach {
            val fqn = FQN(it.data.name)
            val classFile = classFileList.firstOrNull { it.fqn == fqn }
                    ?: ClassFile(it.toClassDec()).apply { classFileList.add(this) }
            classFile.constructors.add(it)
        }

        this.classFileList = classFileList.toSet()

        interfaceFileList = declarations.filterIsInstance(InterfaceDec::class.java)
                .map { InterfaceFile(it) }
                .toSet()

        enumFileList = declarations.filterIsInstance(EnumDec::class.java)
                .map { EnumFile(it) }
                .toSet()

        val generatedData = mutableListOf<GeneratedFile>()
        generatedData.addAll(classFileList)
        generatedData.addAll(interfaceFileList)
        generatedData.addAll(enumFileList)

        declarations.filterIsInstance(Const::class.java).forEach {
            val fqn = FQN(it.className)
            val classFile = generatedData.first { it.fqn == fqn }
            classFile.addItem(it)
        }

        declarations.filterIsInstance(EnumValue::class.java).forEach {
            val fqn = FQN(it.className)
            val classFile = enumFileList.first { it.fqn == fqn }
            classFile.addItem(it)
        }
    }

    fun generate(directory: File) {
        directory.mkdirs()
        directory.deleteRecursively()

        classFileList.forEach { generate(directory, it) }
        interfaceFileList.forEach { generate(directory, it) }
        enumFileList.forEach { generate(directory, it) }
    }

    private fun generate(directory: File, generatedFile: GeneratedFile) {
        val fqn = generatedFile.fqn
        val dir = directory.resolve(fqn.path)
        dir.mkdirs()

        val file = dir.resolve("${fqn.name}.kt")
        file.writeText("${generatedFile.header}\n${generatedFile.content()}")
    }

    class FQN(val fqn: String) {
        private val names = fqn.split(".")
        private val packageNames = names.subList(0, names.size - 1)

        val name = names.last()
        val packageName = packageNames.joinToString(separator = ".")
        val path = packageNames.joinToString(separator = "/")

        override fun equals(other: Any?): Boolean {
            return other is FQN && other.fqn == fqn
        }

        override fun hashCode(): Int {
            return fqn.hashCode()
        }
    }

    abstract class GeneratedFile(private val declaration: InstanceDec) {
        val fqn: FQN = FQN(declaration.data.name)
        protected val items: MutableList<Declaration> = mutableListOf()

        val consts: List<Const>
            get() = items.filterIsInstance(Const::class.java)

        val staticConsts: List<Const>
            get() = consts.filter { it.static }

        val memeberConsts: List<Const>
            get() = consts.filter { !it.static }

        val header: String
            get() = "package ${fqn.packageName}\n"

        fun addItem(item: Declaration) {
            items.add(item)
        }

        fun genericParameters(): String {
            val parameters = declaration.genericParameters()
            if (parameters.isEmpty()) {
                return ""
            }
            return "<${parameters.map { it.name }.joinToString(", ")}>"
        }

        open protected fun isStatic(): Boolean {
            return false
        }

        protected fun companionContent(): String {
            val items = staticConsts.map {
                // TODO: Check. Quick fix for generics in constants
                // One case - IListEnumerable.EMPTY
                val type = it.type.replace("<T>", "<out Any>")
                "        val ${it.name}: $type = noImpl"
            }

            if (items.isEmpty()) {
                return ""
            }

            val result = items.joinToString("\n") + "\n"
            if (isStatic()) {
                return result
            }

            return "    companion object {\n" +
                    result +
                    "    }\n"
        }

        abstract fun content(): String
    }

    class ClassFile(private val declaration: ClassDec) : GeneratedFile(declaration) {
        val constructors: MutableList<Constructor> = mutableListOf()

        override fun isStatic(): Boolean {
            return declaration.static
        }

        private fun type(): String {
            if (isStatic()) {
                return "object"
            }

            // no such cases
            // JS specific?
            if (declaration.abstract) {
                return "abstract class"
            }

            if (declaration.open) {
                return "open class"
            }

            return "class"
        }

        override fun content(): String {
            return "external ${type()} ${fqn.name}${genericParameters()} {\n" +
                    companionContent() +
                    "}"
        }
    }

    class InterfaceFile(declaration: InterfaceDec) : GeneratedFile(declaration) {
        override fun content(): String {
            return "external interface ${fqn.name}${genericParameters()} {\n" +
                    companionContent() +
                    "}\n"
        }
    }

    class EnumFile(declaration: EnumDec) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = items.filterIsInstance(EnumValue::class.java)
                    .map { "    val ${it.name}: ${it.className} = noImpl" }
                    .joinToString("\n")
            return "external object ${fqn.name}: ${ENUM_TYPE} {\n" +
                    values + "\n" +
                    "}\n"
        }
    }
}

object StringUtil {
    fun between(str: String, start: String, end: String, firstEnd: Boolean = false): String {
        val startIndex = str.indexOf(start)
        if (startIndex == -1) {
            throw GradleException("String doesn't contains '$start'")
        }

        val endIndex = if (firstEnd) {
            str.indexOf(end)
        } else {
            str.lastIndexOf(end)
        }
        if (endIndex == -1) {
            throw GradleException("String doesn't contains '$end'")
        }

        return str.substring(startIndex + start.length, endIndex)
    }

    fun hardBetween(str: String, start: String, end: String): String {
        if (!str.startsWith(start)) {
            throw GradleException("String '$this' not started from '$start'")
        }

        if (!str.endsWith(end)) {
            throw GradleException("String '$this' not ended with '$end'")
        }

        return str.substring(start.length, str.length - end.length)
    }

    fun till(str: String, end: String): String {
        val endIndex = str.indexOf(end)
        if (endIndex == -1) {
            throw GradleException("String doesn't contains '$end'")
        }

        return str.substring(0, endIndex)
    }

    fun from(str: String, start: String): String {
        val startIndex = str.lastIndexOf(start)
        if (startIndex == -1) {
            throw GradleException("String doesn't contains '$start'")
        }

        return str.substring(startIndex + start.length)
    }
}