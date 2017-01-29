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

        val NAMESPACE = "@namespace"
        val CLASS = "@class "
        val INTERFACE = "@interface"
        val CONSTRUCTOR = "@constructor"

        val CONST = "@const"
        val STATIC = "@static"
        val FINAL = "@final"
        val ABSTRACT = "@abstract"
        val PROTECTED = "@protected"

        val IMPLEMENTS = "@implements"
        val EXTENDS = "@extends"

        val PARAM = "@param"
        val RETURNS = "@returns"
        val TYPE = "@type"

        val GETS_OR_SETS = "Gets or sets"
        val NULL_VALUE = "null;"

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

            if (lines.contains(CONSTRUCTOR)) {
                return Constructor(data, lines)
            }

            if (lines.contains(CONST)) {
                return Const(data, lines)
            }

            if (data.nullValue) {
                return Property(data, lines)
            }

            return Function(data, lines)
        }

        fun parseType(line: String): String {
            val i1 = line.indexOf("{")
            val i2 = line.indexOf("}")

            return line.substring(i1 + 1, i2)
                    .replace(".<", "<")
                    .replace("object", "yfiles.lang.Object")
                    .replace("boolean", "Boolean")
                    .replace("string", "String")
                    .replace("number", "Number")
        }
    }
}

class Data(val name: String, val value: String) {
    companion object {
        fun parse(source: String): Data {
            val items = source.split("=")
            if (items.size == 1) {
                val name = source.substring(0, source.length - 1)
                return Data(name, Declaration.NULL_VALUE)
            }

            if (items.size != 2) {
                throw GradleException("Invalid declaration: '$source'")
            }

            return Data(items[0], items[1])
        }
    }

    val nullValue: Boolean
        get() = value == Declaration.NULL_VALUE
}

class ClassDec(data: Data, private val lines: List<String>) : Declaration(data) {
    val static = lines.contains(STATIC)
    val open = !lines.contains(FINAL)
    val abstract = lines.contains(ABSTRACT)
}

class InterfaceDec(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Constructor(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Const(data: Data, lines: List<String>) : Declaration(data) {
    val static = lines.contains(STATIC)
    val type: String
    val name: String
    val className: String

    init {
        type = parseType(lines.first { it.startsWith(TYPE) })

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

}

data class Parameter(val name: String, val type: String)

class Namespace(data: Data) : Declaration(data)

class Undefined(data: Data) : Declaration(data)

class FileGenerator(declarations: List<Declaration>) {

    private val classDataList: Set<ClassFile>
    private val interfaceDataList: Set<InterfaceFile>

    init {
        val classDataList = mutableListOf<ClassFile>()
        classDataList.addAll(
                declarations.filter({ it is ClassDec }).map({ ClassFile(FQN(it.data.name)) })
        )

        declarations.filter({ it is Constructor }).forEach {
            val fqn = FQN(it.data.name)
            var classFile = classDataList.firstOrNull { it.fqn == fqn }
            if (classFile == null) {
                classFile = ClassFile(fqn)
                classDataList.add(classFile)
            }
            classFile.constructors.add(it as Constructor)
        }

        this.classDataList = classDataList.toSet()

        interfaceDataList = declarations.filter({ it is InterfaceDec })
                .map { InterfaceFile(it as InterfaceDec) }
                .toSet()

        val generatedData = mutableListOf<GeneratedFile>()
        generatedData.addAll(classDataList)
        generatedData.addAll(interfaceDataList)

        declarations.filter({ it is Const }).forEach {
            val fqn = FQN((it as Const).className)
            val classFile = generatedData.first { it.fqn == fqn }
            classFile.consts.add(it)
        }
    }

    fun generate(directory: File) {
        directory.mkdirs()
        directory.deleteRecursively()

        classDataList.forEach { generate(directory, it) }
        interfaceDataList.forEach { generate(directory, it) }
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

    abstract class GeneratedFile(val fqn: FQN) {
        val consts: MutableList<Const> = mutableListOf()

        val header: String
            get() = "package ${fqn.packageName}\n"

        abstract fun content(): String
    }

    class ClassFile(fqn: FQN) : GeneratedFile(fqn) {
        var declaration: ClassDec? = null
        val constructors: MutableList<Constructor> = mutableListOf()

        private fun isStatic(): Boolean {
            return declaration?.static ?: false
        }

        private fun type(): String {
            return if (isStatic()) "object" else "class"
        }

        override fun content(): String {
            return "external ${type()} ${fqn.name} {\n" +
                    "}"
        }
    }

    class InterfaceFile(val declaration: InterfaceDec) : GeneratedFile(FQN(declaration.data.name)) {
        override fun content(): String {
            val constants = consts.map {
                // TODO: Check. Quick fix for generics in constants
                val type = it.type.replace("<T>", "")
                "val ${it.name}: $type = noImpl"
            }.joinToString("\n")

            return "external interface ${fqn.name} {\n" +
                    "    companion object {\n" +
                    constants + "\n" +
                    "    }\n" +
                    "}\n"
        }
    }
}