import Build_gradle.Hacks.isClassParameter
import Build_gradle.Hacks.validateStaticConstType
import Build_gradle.Types.BEND_TYPE
import Build_gradle.Types.CLASS_TYPE
import Build_gradle.Types.COLUMN_TYPE
import Build_gradle.Types.EDGE_TYPE
import Build_gradle.Types.ENUM_TYPE
import Build_gradle.Types.LABEL_TYPE
import Build_gradle.Types.NODE_TYPE
import Build_gradle.Types.OBJECT_TYPE
import Build_gradle.Types.PORT_TYPE
import Build_gradle.Types.ROW_TYPE
import org.gradle.api.GradleException
import java.io.File
import java.nio.charset.Charset
import java.util.*

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
    val sourceDir = projectDir.resolve("generated/src/main/kotlin")
    fileGenerator.generate(sourceDir)

    // TODO: Check if this class really needed
    sourceDir.resolve("yfiles/lang/String.kt").delete()
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

object Types {
    val OBJECT_TYPE = "yfiles.lang.Object"
    val CLASS_TYPE = "yfiles.lang.Class"
    val ENUM_TYPE = "yfiles.lang.Enum"

    val NODE_TYPE = "yfiles.graph.INode"
    val EDGE_TYPE = "yfiles.graph.IEdge"
    val PORT_TYPE = "yfiles.graph.IPort"
    val LABEL_TYPE = "yfiles.graph.ILabel"
    val BEND_TYPE = "yfiles.graph.IBend"
    val ROW_TYPE = "yfiles.graph.IRow"
    val COLUMN_TYPE = "yfiles.graph.IColumn"
}

open class Declaration(protected val data: Data) {
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
                "number" to "Number",
                "void" to "Unit",
                "Event" to "org.w3c.dom.events.Event",
                "KeyboardEvent" to "org.w3c.dom.events.KeyboardEvent",
                "Node" to "org.w3c.dom.Node",
                "Element" to "org.w3c.dom.Element",
                "HTMLElement" to "org.w3c.dom.HTMLElement",
                "HTMLDivElement" to "org.w3c.dom.HTMLDivElement",
                "SVGElement" to "org.w3c.dom.svg.SVGElement",
                "SVGDefsElement" to "org.w3c.dom.svg.SVGDefsElement"
        )

        fun parse(source: String, lines: List<String>): Declaration {
            val data = Data.parse(source)

            return when {
                lines.contains(NAMESPACE) ->
                    Namespace(data)
                lines.any { it.startsWith(CLASS) } && !lines.contains(CONSTRUCTOR) ->
                    ClassDec(data, lines)
                lines.contains(INTERFACE) ->
                    InterfaceDec(data, lines)
                lines.contains(EXTENDS_ENUM) ->
                    EnumDec(data, lines)
                lines.contains(CONSTRUCTOR) ->
                    Constructor(data, lines)
                lines.contains(CONST) ->
                    Const(data, lines)
                !source.contains("=") ->
                    EnumValue(data, lines)
                data.nullValue ->
                    Property(data, lines)
                else ->
                    Function(data, lines)
            }
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
            // TODO: Fix for SvgDefsManager and SvgVisual required (2 constructors from 1)
            if (type.contains("|")) {
                return parseType(type.split("|")[0])
            }

            val standardType = STANDARD_TYPE_MAP[type]
            if (standardType != null) {
                return standardType
            }

            if (!type.contains(GENERIC_START)) {
                return type
            }

            val mainType = StringUtil.till(type, GENERIC_START)
            val parametrizedTypes = parseGenericParameters(StringUtil.between(type, GENERIC_START, GENERIC_END))
            return "$mainType<${parametrizedTypes.joinToString(", ")}>"
        }

        fun parseParamLine(line: String, function: String): Parameter {
            Hacks.parseParamLine(line, function).apply {
                if (this != null) {
                    return this
                }
            }

            var name = StringUtil.from2(line, "} ").split(" ").get(0)
            var defaultValue: String? = null
            if (name.startsWith("[")) {
                val data = StringUtil.hardBetween(name, "[", "]").split("=")
                name = data[0]
                defaultValue = data[1]
            }
            var rawType = StringUtil.between(line, " {", "} ", true)
            val vararg = rawType.startsWith("...")
            if (vararg) {
                rawType = rawType.substring(3)
            }
            val type = if (rawType.startsWith(FUNCTION_START)) {
                val parameterTypes = StringUtil.between(rawType, FUNCTION_START, FUNCTION_END).split(", ").map { parseType(it) }
                val resultType = parseType(StringUtil.from(rawType, FUNCTION_END))
                "(${parameterTypes.joinToString(", ")}) -> $resultType"
            } else {
                parseType(rawType)
            }
            return Parameter(name, type, defaultValue, vararg)
        }

        fun parseGenericParameters(parameters: String): List<String> {
            // TODO: temp hack for generic, logic check required
            if (!parameters.contains(GENERIC_START)) {
                return parameters.split(",").map { parseType(it) }
            }

            val firstType = firstGenericType(parameters)
            if (firstType == parameters) {
                return listOf(parseType(firstType))
            }

            val types = mutableListOf(firstType)
            types.addAll(parseGenericParameters(parameters.substring(firstType.length + 1)))
            return types.toList()
        }

        fun firstGenericType(parameters: String): String {
            var semafor = 0
            var index = 0

            while (true) {
                val indexes = listOf(
                        parameters.indexOf(",", index),
                        parameters.indexOf(".<", index),
                        parameters.indexOf(">", index)
                )

                if (indexes.all { it == -1 }) {
                    return parameters
                }

                // TODO: check calculation
                index = indexes.map({ if (it == -1) 100000 else it })
                        .minWith(Comparator { o1, o2 -> Math.min(o1, o2) }) ?: -1

                if (index == -1 || index == parameters.lastIndex) {
                    return parameters
                }

                when (indexes.indexOf(index)) {
                    0 -> if (semafor == 0) return parameters.substring(0, index)
                    1 -> semafor++
                    2 -> semafor--
                }
                index++
            }
        }

        fun parseFunctionType(type: String): String {
            val parameterTypes = StringUtil.between(type, FUNCTION_START, FUNCTION_END).split(",").map({ parseType(it) })
            val resultType = parseType(StringUtil.from(type, FUNCTION_END))
            return "(${parameterTypes.joinToString(", ")}) -> $resultType"
        }
    }

    val className: String
    val name: String

    init {
        if (instanceMode()) {
            className = data.name
            name = data.name.split(".").last()
        } else {
            val names = data.name.split(".")
            name = names.last()

            var i = names.size - 2
            if (names[i] == "prototype") {
                i--
            }
            className = names.subList(0, i + 1).joinToString(separator = ".")
        }
    }

    open fun instanceMode(): Boolean {
        return false
    }
}

class Data(val source: String, val name: String, val value: String) {
    companion object {
        fun parse(source: String): Data {
            val items = source.split("=")
            return when (items.size) {
                1 -> {
                    val name = source.substring(0, source.length - 1)
                    Data(source, name, Declaration.NULL_VALUE)
                }
                2 -> Data(source, items[0], items[1])
                else -> throw GradleException("Invalid declaration: '$source'")
            }
        }
    }

    val nullValue: Boolean
        get() = value == Declaration.NULL_VALUE
}

open class InstanceDec(data: Data, protected val lines: List<String>) : Declaration(data) {
    override fun instanceMode(): Boolean {
        return true
    }

    fun genericParameters(): List<GenericParameter> {
        val templateLine = lines.firstOrNull { it.startsWith(TEMPLATE) }
                ?: return emptyList()
        val names = StringUtil.from(templateLine, TEMPLATE).split(",")
        // TODO: support generic type read
        return names.map { GenericParameter(it, "") }
    }

    fun implementedTypes(): List<String> {
        return lines.filter { it.startsWith(IMPLEMENTS) }
                .map { StringUtil.between(it, IMPLEMENTS + "{", "}", true) }
                .map { parseType(it) }
    }
}

class ClassDec(data: Data, lines: List<String>) : InstanceDec(data, lines) {
    val static = lines.contains(STATIC)
    val open = !lines.contains(FINAL)
    val abstract = lines.contains(ABSTRACT)
}

class InterfaceDec(data: Data, lines: List<String>) : InstanceDec(data, lines)

class EnumDec(data: Data, lines: List<String>) : InstanceDec(data, lines)

class Constructor : Function {
    protected constructor(data: Data, lines: List<String>, parameters: Parameters, generated: Boolean)
            : super(data, lines, parameters, generated)

    constructor(data: Data, lines: List<String>) : super(data, lines)

    override fun instanceMode(): Boolean {
        return true
    }

    override fun generateAdapter(parameters: List<Parameter>): Function {
        return Constructor(data, lines, Parameters(parameters), true)
    }

    fun toClassDec(): ClassDec {
        return ClassDec(data, lines)
    }

    // TODO: check why it doesn't work
    /*
    override fun hashCode(): Int {
        return Objects.hash(data.name, parameters)
    }

    override fun equals(other: Any?): Boolean {
        return other is Constructor
                && Objects.equals(data.name, other.data.name)
                && Objects.equals(parameters, other.parameters)
    }
    */

    override fun hashCode(): Int {
        return Objects.hash(data.name, parametersString())
    }

    override fun equals(other: Any?): Boolean {
        return other is Constructor
                && Objects.equals(data.name, other.data.name)
                && Objects.equals(parametersString(), other.parametersString())
    }

    override fun toString(): String {
        if (generated) {
            val generic = Hacks.getGenerics(className)
            return "fun $generic $name.Companion.create(${parametersString()}): $name$generic {\n" +
                    "    return $name(${mapString(parameters)})\n" +
                    "}\n\n"
        }

        val modificator = when {
            protected -> "protected "
            adapter != null -> "internal "
            else -> ""
        }
        return "    ${modificator}constructor(${parametersString()})"
    }
}

class Const(data: Data, lines: List<String>) : Declaration(data) {
    val static = lines.contains(STATIC)
    val type: String

    init {
        type = parseTypeLine(lines.first { it.startsWith(TYPE) })
    }
}

class Property(data: Data, private val lines: List<String>) : Declaration(data)

open class Function : Declaration {
    companion object {
        val START = "function("
        val END = "){};"

        private fun calculateParameters(data: Data, lines: List<String>): Parameters {
            val value = data.value
            val parameterNames = StringUtil.hardBetween(value, START, END).split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (parameterNames.isEmpty()) {
                return Parameters(emptyList())
            }

            val parametersMap = lines.filter { it.startsWith(PARAM) }.map { parseParamLine(it, data.name) }.associate { Pair(it.name, it) }

            if (parametersMap.keys.any { it.contains(".") }) {
                if (parameterNames.size != 1) {
                    throw GradleException("Options parameter available only if parameter is single.")
                }

                val parameterName = parameterNames[0]
                val parameter = parametersMap[parameterName]
                if (parameter?.type != OBJECT_TYPE) {
                    throw GradleException("Options parameter must have type $OBJECT_TYPE.")
                }

                val generatedParameters = parametersMap.values
                        .filter { it.name != parameterName }
                        .map {
                            val name = it.name.split(".")[1]
                            Parameter(name, it.type, it.defaultValue, it.vararg)
                        }

                return Parameters(listOf(Parameter(parameterName, "kotlin.collections.Map<String, Any?>", parameter.defaultValue)), generatedParameters)
            }

            val parameters = parameterNames.map { name ->
                val parameter = parametersMap.get(name)
                when {
                    parameter != null -> parameter
                    isClassParameter(name) -> Parameter(name, CLASS_TYPE)
                    else -> throw GradleException("No type info about parameter '$name' in function\n'${data.name}'")
                }
            }

            return Parameters(parameters)
        }
    }

    val adapter: Function?

    protected val static: Boolean
    protected val protected: Boolean
    protected val lines: List<String>
    protected val parameters: List<Parameter>
    protected val generated: Boolean

    protected constructor(data: Data, lines: List<String>, parameters: Parameters, generated: Boolean) : super(data) {
        this.static = lines.contains(STATIC)
        this.protected = lines.contains(PROTECTED)
        this.lines = lines
        this.parameters = parameters.items
        this.generated = generated

        adapter = if (parameters.generatedItems != null) {
            generateAdapter(parameters.generatedItems)
        } else {
            null
        }
    }

    constructor(data: Data, lines: List<String>) : this(data, lines, calculateParameters(data, lines), false)

    open fun generateAdapter(parameters: List<Parameter>): Function {
        return Function(data, lines, Parameters(parameters), true)
    }

    protected fun mapString(parameters: List<Parameter>): String {
        return "mapOf<String, Any?>(\n" +
                parameters.map { "\"${it.name}\" to ${it.name}" }.joinToString(",\n") +
                "\n)\n"
    }

    protected fun parametersString(): String {
        return parameters
                .map { "${it.name}: ${it.type}" }
                .joinToString(", ")
    }

    override fun toString(): String {
        val functionName = data.name.split(".").last()

        if (generated) {
            TODO("Generated function logic not implemented!")
            /*
            return " {\n" +
                    "$name(${mapString(parameters)})\n" +
                    "}\n\n"
            */
        }
        return "fun $functionName(${parametersString()}) = definedExternally"
    }
}

class EnumValue(data: Data, private val lines: List<String>) : Declaration(data)

class Parameters(val items: List<Parameter>, val generatedItems: List<Parameter>? = null)

data class Parameter(val name: String, val type: String, val defaultValue: String? = null, val vararg: Boolean = false)
data class GenericParameter(val name: String, val type: String)

class Namespace(data: Data) : Declaration(data) {
    override fun instanceMode(): Boolean {
        return true
    }
}

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
            val className = it.className
            val classFile = classFileList.firstOrNull { it.className == className }
                    ?: ClassFile(it.toClassDec()).apply { classFileList.add(this) }
            classFile.addItem(it)
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

        declarations.filterNot { it is Namespace || it is InstanceDec || it is Constructor }
                .forEach {
                    val className = it.className
                    val classFile = generatedData.first { it.className == className }
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
        val className = declaration.className
        val fqn: FQN = FQN(className)
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

        protected fun parentTypes(): List<String> {
            return declaration.implementedTypes()
        }

        protected fun parentString(): String {
            val parentTypes = parentTypes()
            if (parentTypes.isEmpty()) {
                return ""
            }
            return ": " + parentTypes.joinToString(", ")
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
                val type = validateStaticConstType(it.type)
                "        val ${it.name}: $type = definedExternally"
            }

            if (items.isEmpty()) {
                return when {
                    isStatic() -> ""
                // TODO: add companion only if needed
                    else -> "    companion object {}\n"
                }
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

        private fun constructors(): String {
            val constructorSet = items.filterIsInstance(Constructor::class.java).toSet()
            return constructorSet.map {
                it.toString()
            }.joinToString("\n") + "\n"
        }

        private fun adapters(): String {
            val constructorAdapters = Hacks.filterConstructorAdapters(
                    className,
                    items.filterIsInstance(Constructor::class.java)
                            .mapNotNull { it.adapter as? Constructor }
            )

            return constructorAdapters.map {
                it.toString()
            }.joinToString("\n") + "\n"
        }

        override fun content(): String {
            return "external ${type()} ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    companionContent() +
                    constructors() +
                    "}\n\n" +
                    adapters()
        }
    }

    class InterfaceFile(declaration: InterfaceDec) : GeneratedFile(declaration) {
        override fun content(): String {
            return "external interface ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    companionContent() +
                    "}\n"
        }
    }

    class EnumFile(declaration: EnumDec) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = items.filterIsInstance(EnumValue::class.java)
                    .map { "    val ${it.name}: ${it.className} = definedExternally" }
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
            throw GradleException("String '$str' doesn't contains '$start'")
        }

        val endIndex = if (firstEnd) {
            str.indexOf(end)
        } else {
            str.lastIndexOf(end)
        }
        if (endIndex == -1) {
            throw GradleException("String '$str' doesn't contains '$end'")
        }

        return str.substring(startIndex + start.length, endIndex)
    }

    fun hardBetween(str: String, start: String, end: String): String {
        if (!str.startsWith(start)) {
            throw GradleException("String '$str' not started from '$start'")
        }

        if (!str.endsWith(end)) {
            throw GradleException("String '$str' not ended with '$end'")
        }

        return str.substring(start.length, str.length - end.length)
    }

    fun till(str: String, end: String): String {
        val endIndex = str.indexOf(end)
        if (endIndex == -1) {
            throw GradleException("String '$str' doesn't contains '$end'")
        }

        return str.substring(0, endIndex)
    }

    fun from(str: String, start: String): String {
        val startIndex = str.lastIndexOf(start)
        if (startIndex == -1) {
            throw GradleException("String '$str' doesn't contains '$start'")
        }

        return str.substring(startIndex + start.length)
    }

    fun from2(str: String, start: String): String {
        val startIndex = str.indexOf(start)
        if (startIndex == -1) {
            throw GradleException("String '$str' doesn't contains '$start'")
        }

        return str.substring(startIndex + start.length)
    }
}

object Hacks {
    fun parseParamLine(line: String, function: String): Parameter? {
        if (line.startsWith("@param value")) {
            return when (function) {
                "yfiles.collections.IList.prototype.set" -> Parameter("value", "T")
                "yfiles.collections.IMapper.prototype.set" -> Parameter("value", "V")
                "yfiles.graphml.CreationProperties.prototype.set" -> Parameter("value", OBJECT_TYPE)
                "yfiles.algorithms.YList.prototype.set" -> Parameter("value", OBJECT_TYPE)
                "yfiles.collections.IMap.prototype.set" -> Parameter("value", "TValue")
                else -> throw GradleException("No hacked parameter value for function $function")
            }
        }

        if (line.startsWith("@param options.")) {
            val name = line.split(" ")[1]

            return when {
                function == "yfiles.collections.Map" && name == "options.entries" -> Parameter(name, "Array<MapEntry<TKey, TValue>>")
                function == "yfiles.collections.List" && name == "options.items" -> Parameter(name, "Array<T>")
                function == "yfiles.view.LinearGradient" || function == "yfiles.view.RadialGradient"
                        && name == "options.gradientStops" -> Parameter(name, "Array<yfiles.view.GradientStop>")
                function == "yfiles.graph.IGraph.prototype.createGroupNode" && name == "options.children" -> Parameter(name, "Array<$NODE_TYPE>")
                function == "yfiles.graph.ITable.prototype.createChildRow" && name == "options.childRows" -> Parameter(name, "Array<$ROW_TYPE>")
                function == "yfiles.graph.ITable.prototype.createChildColumn" && name == "options.childColumns" -> Parameter(name, "Array<$COLUMN_TYPE>")
                name == "options.nodes" -> Parameter(name, NODE_TYPE)
                name == "options.edges" -> Parameter(name, EDGE_TYPE)
                name == "options.ports" -> Parameter(name, PORT_TYPE)
                name == "options.labels" -> Parameter(name, LABEL_TYPE)
                name == "options.bends" -> Parameter(name, BEND_TYPE)
                else -> throw GradleException("No hacked parameter '$name' for function $function")
            }
        }

        return null
    }

    fun validateStaticConstType(type: String): String {
        // TODO: Check. Quick fix for generics in constants
        // One case - IListEnumerable.EMPTY
        return type.replace("<T>", "<out Any>")
    }

    fun isClassParameter(name: String): Boolean {
        return name.endsWith("Type")
    }

    // TODO: get generics from class
    fun getGenerics(className: String): String {
        return when (className) {
            "yfiles.collections.List" -> "<T>"
            "yfiles.collections.Map" -> "<TKey, TValue>"
            "yfiles.collections.Mapper" -> "<K, V>"
            else -> ""
        }
    }

    fun filterConstructorAdapters(className: String, adapters: List<Constructor>): List<Constructor> {
        return when (className) {
            "yfiles.styles.NodeStylePortStyleAdapter" -> adapters.toSet().toList()
            else -> adapters
        }
    }
}