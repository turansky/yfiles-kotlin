import Build_gradle.Hacks.getAdditionalContent
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
import Build_gradle.Types.UNIT
import org.gradle.api.GradleException
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KProperty

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.json:json:20160810")
    }
}

task("build") {
    // val source = project.properties["apiFile"] ?: throw GradleException("Invalid 'apiFile' parameter value!")
    val source = "descriptors/yfiles/2.0.0.1/api.json"
    val file = file(source) ?: throw GradleException("No file located in '$source'")
    generateKotlinWrappers(file)
}

fun generateKotlinWrappers(sourceFile: File) {
    val source = JSONObject(sourceFile.readText(Charset.forName("UTF-8")))
    val apiRoot = JAPIRoot(source)

    apiRoot.namespaces.forEach {
        println("Namespace: ${it.id}")
    }

    /*
    val declarations = mutableListOf<Declaration>()
    while (lines.hasNext()) {
        val declaration = DeclarationReader.read(lines)
        declarations.add(declaration)
    }

    val classes = declarations.filterIsInstance(ClassDec::class.java).map { it.className }
            .toMutableSet()

    val additionalClasses = declarations.filterIsInstance(Constructor::class.java)
            .mapNotNull {
                val className = it.className
                if (classes.contains(className)) {
                    null
                } else {
                    classes.add(className)
                    it.toClassDec()
                }
            }

    declarations.addAll(additionalClasses)

    declarations.removeIf { Hacks.redundantDeclaration(it) }

    val classRegistry = ClassRegistryImpl(declarations)
    declarations.forEach {
        it.classRegistry = classRegistry
    }

    val fileGenerator = FileGenerator(declarations)
    val sourceDir = projectDir.resolve("generated/src/main/kotlin")
    fileGenerator.generate(sourceDir)

    // TODO: Check if this class really needed
    sourceDir.resolve("yfiles/lang/Boolean.kt").delete()
    sourceDir.resolve("yfiles/lang/Number.kt").delete()
    sourceDir.resolve("yfiles/lang/String.kt").delete()
    sourceDir.resolve("yfiles/lang/Struct.kt").delete()
    */
}

abstract class JsonWrapper(val source: JSONObject)

class JAPIRoot(source: JSONObject) : JsonWrapper(source) {
    val namespaces: List<JNamespace> by ArrayDelegate({ JNamespace(it) })
}

class JNamespace(source: JSONObject) : JsonWrapper(source) {
    val id: String by StringDelegate()
    val name: String by StringDelegate()

    val namespaces: List<JNamespace> by ArrayDelegate({ JNamespace(it) })
    val types: List<JType> by ArrayDelegate({ JType(it) })
}

class JType(source: JSONObject) : JsonWrapper(source) {
    val id: String by StringDelegate()
    val name: String by StringDelegate()
    val modifiers: List<String> by StringArrayDelegate()

    val group: String by StringDelegate()
    val summary: String by StringDelegate()
    val remarks: String by StringDelegate()
}

class ArrayDelegate<T>(private val transform: (JSONObject) -> T) {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): List<T> {
        val array = thisRef.source.getJSONArray(property.name)
        val length = array.length()
        if (length == 0) {
            return emptyList()
        }

        val list = mutableListOf<T>()
        for (i in 0..length - 1) {
            list.add(transform(array.getJSONObject(i)))
        }
        return list.toList()
    }
}

class StringArrayDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): List<String> {
        val array = thisRef.source.getJSONArray(property.name)
        val length = array.length()
        if (length == 0) {
            return emptyList()
        }

        val list = mutableListOf<String>()
        for (i in 0..length - 1) {
            list.add(array.getString(i))
        }
        return list.toList()
    }
}

class StringDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String {
        return thisRef.source.getString(property.name)
    }
}

object Types {
    val UNIT = "Unit"
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

interface ClassRegistry {
    fun isInterface(className: String): Boolean
    fun isFinalClass(className: String): Boolean
    fun isGetterSetter(className: String, propertyName: String): Boolean
    fun functionOverriden(className: String, functionName: String): Boolean
    fun propertyOverriden(className: String, functionName: String): Boolean
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

        val GETS_OR_SETS = "Gets or sets "
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
                "Date" to "kotlin.js.Date",
                "void" to UNIT,
                "Function" to "() -> $UNIT",

                "Event" to "org.w3c.dom.events.Event",
                "KeyboardEvent" to "org.w3c.dom.events.KeyboardEvent",
                "Document" to "org.w3c.dom.Document",
                "Node" to "org.w3c.dom.Node",
                "Element" to "org.w3c.dom.Element",
                "HTMLElement" to "org.w3c.dom.HTMLElement",
                "HTMLInputElement" to "org.w3c.dom.HTMLInputElement",
                "HTMLDivElement" to "org.w3c.dom.HTMLDivElement",
                "SVGElement" to "org.w3c.dom.svg.SVGElement",
                "SVGDefsElement" to "org.w3c.dom.svg.SVGDefsElement",
                "SVGGElement" to "org.w3c.dom.svg.SVGGElement",
                "SVGImageElement" to "org.w3c.dom.svg.SVGImageElement",
                "SVGPathElement" to "org.w3c.dom.svg.SVGPathElement",
                "SVGTextElement" to "org.w3c.dom.svg.SVGTextElement",
                "CanvasRenderingContext2D" to "org.w3c.dom.CanvasRenderingContext2D",

                // TODO: check if Kotlin promises is what we need in yFiles
                "Promise" to "kotlin.js.Promise"
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

        fun findType(line: String): String {
            return StringUtil.between(line, "{", "}", true)
        }

        fun parseTypeLine(line: String): String {
            val type = findType(line)
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

            val mainType = parseType(StringUtil.till(type, GENERIC_START))
            val parametrizedTypes = parseGenericParameters(StringUtil.between(type, GENERIC_START, GENERIC_END))
            return "$mainType<${parametrizedTypes.joinToString(", ")}>"
        }

        fun getReturnType(lines: List<String>, className: String, name: String): String {
            val line = lines.firstOrNull({ it.startsWith(RETURNS) }) ?: return UNIT
            val hackType = Hacks.getReturnType(line, className, name)
            if (hackType != null) {
                return hackType
            }
            return parseTypeLine(line)
        }

        fun parseGenericParameters(lines: List<String>): List<GenericParameter> {
            val templateLine = lines.firstOrNull { it.startsWith(TEMPLATE) }
                    ?: return emptyList()
            val names = StringUtil.from(templateLine, TEMPLATE).split(",")
            // TODO: support generic type read
            return names.map { GenericParameter(it, "") }
        }

        fun getGenericString(lines: List<String>): String {
            val parameters = parseGenericParameters(lines)
            if (parameters.isEmpty()) {
                return ""
            }
            return "<${parameters.map { it.toString() }.joinToString(", ")}> "
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
                return if (parameters.contains(FUNCTION_START)) {
                    // TODO: realize full logic if needed
                    parameters.split(delimiters = ",", limit = 2).map {
                        if (it.startsWith(FUNCTION_START)) parseFunctionType(it) else parseType(it)
                    }
                } else {
                    parameters.split(",").map { parseType(it) }
                }
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

    protected var _classRegistry: ClassRegistry? = null

    var classRegistry: ClassRegistry
        get() = _classRegistry ?: throw GradleException("Class registry not initialized!")
        set(value) {
            _classRegistry = value
        }

    val shortClassName: String
    val className: String
    val name: String

    init {
        if (instanceMode()) {
            this.className = data.name
            this.name = className.split(".").last()
        } else {
            val names = data.name.split(".")
            this.name = names.last()

            var i = names.size - 2
            if (names[i] == "prototype") {
                i--
            }
            this.className = names.subList(0, i + 1).joinToString(separator = ".")
        }

        this.shortClassName = className.split(".").last()
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

    fun genericParameters(): String {
        return getGenericString(lines)
    }

    fun extendedType(): String? {
        if (Hacks.ignoreExtendedType(className)) {
            return null
        }

        val line = lines.firstOrNull { it.startsWith(EXTENDS) } ?: return null
        return parseType(findType(line))
    }

    fun implementedTypes(): List<String> {
        var types = Hacks.getImplementedTypes(className)
        if (types != null) {
            return types
        }

        types = lines.filter { it.startsWith(IMPLEMENTS) }
                .map { parseType(findType(it)) }

        return MixinHacks.getImplementedTypes(className, types)
    }
}

class ClassDec(data: Data, lines: List<String>) : InstanceDec(data, lines) {
    val static = lines.contains(STATIC)
    val final = lines.contains(FINAL)
    val open = !final
    val abstract = lines.contains(ABSTRACT)

    val modificator = when {
        abstract -> "abstract" // no such cases (JS specific?)
        open -> "open"
        else -> ""
    }
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

    override fun overridden(): Boolean {
        return false
    }

    fun toClassDec(): ClassDec {
        return ClassDec(data, lines)
    }

    override fun toString(): String {
        if (generated) {
            val generic = Hacks.getGenerics(className)
            return "fun $generic $name.Companion.create(${parametersString()}): $name$generic {\n" +
                    "    return $name(${mapString(parameters)})\n" +
                    "}\n\n"
        }

        return "    ${modificator(false)}constructor(${parametersString()})"
    }
}

class Const(data: Data, lines: List<String>) : Declaration(data) {
    val static = lines.contains(STATIC)
    val protected = lines.contains(PROTECTED)
    val type: String

    init {
        type = validateStaticConstType(parseTypeLine(lines.first { it.startsWith(TYPE) }))
    }

    override fun toString(): String {
        val modifier = if (protected) "protected " else ""
        val mode = if (static) "" else "get()"
        return "    ${modifier}val $name: $type $mode = definedExternally"
    }
}

class Property(data: Data, lines: List<String>) : Declaration(data) {
    val static: Boolean
    val protected: Boolean
    val abstract: Boolean

    val getterSetter: Boolean
    private val type: String

    init {
        this.getterSetter = lines.any { it.startsWith(GETS_OR_SETS) }
        val line = lines.firstOrNull { it.startsWith(TYPE) }
        if (line != null) {
            this.type = parseTypeLine(line)
            this.static = lines.contains(STATIC)
            this.protected = lines.contains(PROTECTED)
            this.abstract = lines.contains(ABSTRACT)
        } else {
            this.type = className
            this.static = true
            this.protected = false
            this.abstract = false
        }
    }

    override fun toString(): String {
        val getterSetter = this.getterSetter || classRegistry.isGetterSetter(className, name)

        var str = ""

        if (classRegistry.propertyOverriden(className, name)) {
            str += "override "
        } else {
            if (protected) {
                str += "protected "
            }

            str += when {
                abstract -> "abstract "
                !static && !classRegistry.isFinalClass(className) -> "open "
                else -> ""
            }
        }

        str += if (getterSetter) "var " else "val "

        str += "$name: $type"
        if (!abstract) {
            str += "\n    get() = definedExternally"
            if (getterSetter) {
                str += "\n    set(value) = definedExternally"
            }
        }
        return str
    }
}

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

    val static: Boolean
    val protected: Boolean
    val abstract: Boolean
    protected val lines: List<String>
    protected val parameters: List<Parameter>
    protected val generated: Boolean

    private val returnType: String
    private val generics: String

    protected constructor(data: Data, lines: List<String>, parameters: Parameters, generated: Boolean) : super(data) {
        this.static = lines.contains(STATIC)
        this.protected = lines.contains(PROTECTED)
        this.abstract = lines.contains(ABSTRACT)
        this.lines = lines
        this.parameters = parameters.items.map {
            val newName = Hacks.fixParameterName(className, name, it.name)
            if (newName == it.name) it else Parameter(newName, it.type, it.defaultValue, it.vararg)
        }
        this.generated = generated

        this.generics = Hacks.getFunctionGenerics(className, name) ?: getGenericString(lines)
        this.returnType = getReturnType(lines, className, name)

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

    protected fun parametersString(useDefaultValue: Boolean = true): String {
        return parameters
                .map {
                    var str = "${it.name}: "
                    if (it.vararg) {
                        str = "vararg " + str
                    }
                    str += it.type
                    val defaultValue = it.defaultValue
                    if (defaultValue != null) {
                        // TODO: fix compilation hack
                        if (defaultValue == "null") {
                            str += "?"
                        }
                        if (useDefaultValue) {
                            str += when {
                                _classRegistry?.functionOverriden(className, name) ?: false -> ""
                            // generated || classRegistry.isInterface(className) -> " = $defaultValue"
                                generated -> " = $defaultValue"
                                else -> " = definedExternally"
                            }
                        }
                    }
                    str
                }
                .joinToString(", ")
    }

    open protected fun overridden(): Boolean {
        if (generated) {
            return false
        }

        return classRegistry.functionOverriden(className, name)
    }

    protected fun modificator(canBeOpen: Boolean = true): String {
        // TODO: add abstract modificator if needed
        if (overridden()) {
            return "override "
        }

        val result = when {
            abstract -> "abstract "
            canBeOpen && !static && !classRegistry.isFinalClass(className) -> "open "
            else -> ""
        }
        return result + when {
            protected -> "protected "
            else -> ""
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(className, name, parametersString(false))
    }

    override fun equals(other: Any?): Boolean {
        return other is Function
                && Objects.equals(className, other.className)
                && Objects.equals(name, other.name)
                && Objects.equals(parametersString(false), other.parametersString(false))
    }

    override fun toString(): String {
        if (generated) {
            val instanceName = shortClassName + if (static) ".Companion" else ""
            return "${modificator()}fun $generics $instanceName.$name(${parametersString()}): $returnType {\n" +
                    "    return $name(${mapString(parameters)})\n" +
                    "}\n\n"
        }

        val body = if (abstract) "" else " = definedExternally"
        return "    ${modificator()}fun $generics$name(${parametersString()}): $returnType$body"
    }
}

class EnumValue(data: Data, private val lines: List<String>) : Declaration(data)

class Parameters(val items: List<Parameter>, val generatedItems: List<Parameter>? = null)

data class Parameter(val name: String, val type: String, val defaultValue: String? = null, val vararg: Boolean = false)

class GenericParameter(private val name: String, private val type: String) {
    override fun toString(): String {
        // TODO: add type
        return name
    }
}

class Namespace(data: Data) : Declaration(data) {
    override fun instanceMode(): Boolean {
        return true
    }
}

class ClassRegistryImpl(declarations: List<Declaration>) : ClassRegistry {
    private val instances = declarations.filterIsInstance(InstanceDec::class.java)
            .associateBy({ it.className }, { it })

    private val functions = declarations.filterIsInstance(Function::class.java)
            .filter { it !is Constructor }

    private val properties = declarations.filterIsInstance(Property::class.java)

    private val functionsMap = instances.values.associateBy(
            { it.className },
            { instance -> functions.filter { it.className == instance.className }.map { it.name } }
    )

    private val propertiesMap = instances.values.associateBy(
            { it.className },
            { instance -> properties.filter { it.className == instance.className }.map { it.name } }
    )

    private val propertiesMap2 = instances.values.associateBy(
            { it.className },
            { instance -> properties.filter { it.className == instance.className }.associateBy({ it.name }, { it.getterSetter }) }
    )

    private fun getParents(className: String): List<String> {
        val instance = instances[className] ?: throw GradleException("Unknown instance type: $className")

        return mutableListOf<String>()
                .union(listOf(instance.extendedType()).filterNotNull())
                .union(instance.implementedTypes())
                .map { if (it.contains("<")) StringUtil.till(it, "<") else it }
                .toList()
    }

    private fun isGetterSetter(className: String, propertyName: String, checkCurrentClass: Boolean): Boolean {
        if (checkCurrentClass) {
            val props = propertiesMap2[className] ?: throw GradleException("No properties found for type: $className")
            if (props[propertyName] ?: false) {
                return true
            }
        }
        return getParents(className).any {
            propertyOverriden(it, propertyName, true)
        }
    }

    private fun functionOverriden(className: String, functionName: String, checkCurrentClass: Boolean): Boolean {
        if (checkCurrentClass) {
            val funs = functionsMap[className] ?: throw GradleException("No functions found for type: $className")
            if (funs.contains(functionName)) {
                return true
            }
        }
        return getParents(className).any {
            functionOverriden(it, functionName, true)
        }
    }

    private fun propertyOverriden(className: String, propertyName: String, checkCurrentClass: Boolean): Boolean {
        if (checkCurrentClass) {
            val props = propertiesMap[className] ?: throw GradleException("No properties found for type: $className")
            if (props.contains(propertyName)) {
                return true
            }
        }
        return getParents(className).any {
            propertyOverriden(it, propertyName, true)
        }
    }

    override fun isInterface(className: String): Boolean {
        return instances[className] is InterfaceDec
    }

    override fun isFinalClass(className: String): Boolean {
        val instance = instances[className]
        return instance is ClassDec && instance.final
    }

    override fun isGetterSetter(className: String, propertyName: String): Boolean {
        return isGetterSetter(className, propertyName, false)
    }

    override fun functionOverriden(className: String, functionName: String): Boolean {
        return functionOverriden(className, functionName, false)
    }

    override fun propertyOverriden(className: String, functionName: String): Boolean {
        return propertyOverriden(className, functionName, false)
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
            val classFile = classFileList.first { it.className == className }
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
                    .sortedBy { it.name }

        val functions: List<Function>
            get() = items.filterIsInstance(Function::class.java)
                    .filter { it !is Constructor }
                    .sortedBy { it.name }

        val properties: List<Property>
            get() = items.filterIsInstance(Property::class.java)
                    .sortedBy { it.name }

        val staticConsts: List<Const>
            get() = consts.filter { it.static }

        val staticProperties: List<Property>
            get() = properties.filter { it.static }

        val staticFunctions: List<Function>
            get() = functions.filter { it.static }

        val staticDeclarations: List<Declaration>
            get() {
                return mutableListOf<Declaration>()
                        .union(staticConsts)
                        .union(staticProperties)
                        .union(staticFunctions.toSet())
                        .toList()
            }

        val memberConsts: List<Const>
            get() = consts.filter { !it.static }

        val memberProperties: List<Property>
            get() = properties.filter { !it.static }

        val memberFunctions: List<Function>
            get() = functions.filter { !it.static }

        val header: String
            get() = "package ${fqn.packageName}\n"

        fun addItem(item: Declaration) {
            items.add(item)
        }

        open protected fun parentTypes(): List<String> {
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
            return declaration.genericParameters()
        }

        open protected fun isStatic(): Boolean {
            return false
        }

        protected fun companionContent(): String {
            val items = staticDeclarations.map {
                it.toString()
            }

            if (items.isEmpty()) {
                return when {
                    isStatic() -> ""
                // TODO: add companion only if needed
                    else -> "    companion object \n\n"
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

        open fun content(): String {
            return listOf<Declaration>()
                    .union(memberConsts)
                    .union(memberProperties)
                    .union(memberFunctions)
                    .union(listOf(getAdditionalContent(declaration.className, declaration.extendedType())))
                    .joinToString("\n") + "\n"
        }
    }

    class ClassFile(private val declaration: ClassDec) : GeneratedFile(declaration) {
        override fun isStatic(): Boolean {
            return declaration.static
        }

        private fun type(): String {
            if (isStatic()) {
                return "object"
            }

            val modificator = if (memberFunctions.any { it.abstract } || memberProperties.any { it.abstract }) {
                "abstract"
            } else {
                declaration.modificator
            }

            return modificator + " class"
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

            val staticFunctionAdapters = items.filterIsInstance(Function::class.java)
                    .filter { it.static }
                    .mapNotNull { it.adapter }
                    .filterNot { staticFunctions.contains(it) }

            val adapters = mutableListOf<Declaration>()
                    .union(constructorAdapters)
                    .union(staticFunctionAdapters)
                    .toList()
            return adapters.map {
                var text = it.toString()
                if (isStatic()) {
                    text = text.replace(".Companion.", ".")
                }
                text
            }.joinToString("\n") + "\n"
        }

        override fun parentTypes(): List<String> {
            val extendedType = declaration.extendedType()
            if (extendedType == null) {
                return super.parentTypes()
            }

            return listOf(extendedType)
                    .union(super.parentTypes())
                    .toList()
        }

        override fun content(): String {
            return "external ${type()} ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    companionContent() +
                    constructors() +
                    super.content() + "\n" +
                    "}\n\n" +
                    adapters()
        }
    }

    class InterfaceFile(declaration: InterfaceDec) : GeneratedFile(declaration) {
        override fun content(): String {
            var content = super.content()
            val likeAbstractClass = MixinHacks.defineLikeAbstractClass(className, memberFunctions, memberProperties)
            if (!likeAbstractClass) {
                content = content.replace("abstract ", "")
                        .replace("open fun", "fun")
                        .replace("\n    get() = definedExternally", "")
                        .replace("\n    set(value) = definedExternally", "")
                        .replace(" = definedExternally", "")
            }

            val type = if (likeAbstractClass) "abstract class" else "interface"
            return "external $type ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    companionContent() +
                    content + "\n" +
                    "}\n"
        }
    }

    class EnumFile(declaration: EnumDec) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = items.filterIsInstance(EnumValue::class.java)
                    .map { "    val ${it.name}: ${it.className} = definedExternally" }
                    .joinToString("\n")
            return "external object ${fqn.name}: ${ENUM_TYPE} {\n" +
                    values + "\n\n" +
                    super.content() + "\n" +
                    "}\n"
        }
    }

    class JsInfo {
        private val COMPLETE = "yfiles/complete"
        private val VIEW = "yfiles/view"
        private val LAYOUT = "yfiles/layout"

        private val LANG = "yfiles/lang"

        private val VIEW_COMPONENT = "yfiles/view-component"
        private val VIEW_EDITOR = "yfiles/view-editor"
        private val VIEW_FOLDING = "yfiles/view-folding"
        private val VIEW_TABLE = "yfiles/view-table"
        private val VIEW_GRAPHML = "yfiles/view-graphml"
        private val VIEW_LAYOUT_BRIDGE = "yfiles/view-layout-bridge"
        private val ALGORITHMS = "yfiles/algorithms"
        private val LAYOUT_TREE = "yfiles/layout-tree"
        private val LAYOUT_ORGANIC = "yfiles/layout-organic"
        private val LAYOUT_HIERARCHIC = "yfiles/layout-hierarchic"
        private val LAYOUT_ORTHOGONAL = "yfiles/layout-orthogonal"
        private val LAYOUT_ORTHOGONAL_COMPACT = "yfiles/layout-orthogonal-compact"
        private val LAYOUT_FAMILYTREE = "yfiles/layout-familytree"
        private val LAYOUT_MULTIPAGE = "yfiles/layout-multipage"
        private val LAYOUT_RADIAL = "yfiles/layout-radial"
        private val LAYOUT_SERIESPARALLEL = "yfiles/layout-seriesparallel"
        private val ROUTER_POLYLINE = "yfiles/router-polyline"
        private val ROUTER_OTHER = "yfiles/router-other"
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
    val SYSTEM_FUNCTIONS = listOf("hashCode", "toString")

    fun redundantDeclaration(declaration: Declaration): Boolean {
        if (declaration !is Function) {
            return false
        }

        if (declaration.className == OBJECT_TYPE) {
            return declaration.name in SYSTEM_FUNCTIONS
        }

        return false
    }

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

    fun getFunctionGenerics(className: String, name: String): String? {
        return when {
            className == "yfiles.collections.List" && name == "fromArray" -> "<T>"
            else -> null
        }
    }

    fun filterConstructorAdapters(className: String, adapters: List<Constructor>): List<Constructor> {
        return when (className) {
            "yfiles.styles.NodeStylePortStyleAdapter" -> adapters.toSet().toList()
            else -> adapters
        }
    }

    fun getReturnType(line: String, className: String, name: String): String? {
        if (line.startsWith("@returns {")) {
            return when {
                className == "yfiles.algorithms.EdgeList" && name == "getEnumerator" -> "yfiles.collections.IEnumerator<yfiles.lang.Object>"
                className == "yfiles.algorithms.NodeList" && name == "getEnumerator" -> "yfiles.collections.IEnumerator<yfiles.lang.Object>"
                else -> null
            }
        }

        return when {
            className == "yfiles.input.ConstrainedReshapeHandler" && name == "handleReshape" -> UNIT
            className == "yfiles.input.ConstrainedDragHandler" && name == "handleMove" -> UNIT
            className == "yfiles.geometry.MutableSize" && name == "MutableSize" -> ""
        // TODO: check (in official doc no return type)
            className == "yfiles.geometry.OrientedRectangle" && name == "moveBy" -> "Boolean"
            else -> {
                println("className == \"$className\" && name == \"$name\" -> \"\"")
                println(line)
                throw GradleException("No return type founded!")
            }
        }
    }

    fun ignoreExtendedType(className: String): Boolean {
        return when (className) {
            "yfiles.lang.Exception" -> true
            else -> false
        }
    }

    fun getImplementedTypes(className: String): List<String>? {
        return when (className) {
            "yfiles.algorithms.EdgeList" -> emptyList()
            "yfiles.algorithms.NodeList" -> emptyList()
            else -> null
        }
    }

    val CLONE_REQUIRED = listOf(
            "yfiles.geometry.Matrix",
            "yfiles.geometry.MutablePoint",
            "yfiles.geometry.MutableSize"
    )

    val INVALID_PLACERS = listOf(
            "yfiles.tree.AssistantNodePlacer",
            "yfiles.tree.BusNodePlacer",
            "yfiles.tree.DelegatingNodePlacer",
            "yfiles.tree.DoubleLineNodePlacer",
            "yfiles.tree.FreeNodePlacer",
            "yfiles.tree.GridNodePlacer",
            "yfiles.tree.LayeredNodePlacer",
            "yfiles.tree.LeftRightNodePlacer",
            "yfiles.tree.SimpleNodePlacer"
    )

    val MULTI_STAGE_LAYOUT_CLASSES = listOf(
            "yfiles.layout.GraphTransformer",
            "yfiles.tree.BalloonLayout",
            "yfiles.genealogy.FamilyTreeLayout",
            "yfiles.hierarchic.HierarchicLayout",
            "yfiles.hierarchic.HierarchicLayoutCore",
            "yfiles.circular.CircularLayout",
            "yfiles.circular.SingleCycleLayout",
            "yfiles.organic.ClassicOrganicLayout",
            "yfiles.organic.OrganicLayout",
            "yfiles.orthogonal.OrthogonalLayout",
            "yfiles.radial.RadialLayout",
            "yfiles.seriesparallel.SeriesParallelLayout",
            "yfiles.tree.AspectRatioTreeLayout",
            "yfiles.tree.ClassicTreeLayout",
            "yfiles.tree.TreeLayout"
    )

    private val CLONE_OVERRIDE = "override fun clone(): $OBJECT_TYPE = definedExternally"

    fun getAdditionalContent(className: String, baseClassName: String?): String {

        return when {
            baseClassName == "yfiles.layout.LayoutData"
            -> "override fun apply(layoutGraphAdapter: yfiles.layout.LayoutGraphAdapter, layout: yfiles.layout.ILayoutAlgorithm, layoutGraph: yfiles.layout.CopiedLayoutGraph): Unit = definedExternally"

            className == "yfiles.algorithms.YList"
            -> lines("override val isReadOnly: Boolean",
                    "    get() = definedExternally",
                    "override fun add(item: $OBJECT_TYPE) = definedExternally")

            className in CLONE_REQUIRED
            -> CLONE_OVERRIDE

            baseClassName == "yfiles.tree.RotatableNodePlacerBase"
            -> lines("override fun determineChildConnector(child: yfiles.algorithms.Node): yfiles.tree.ParentConnectorDirection = definedExternally",
                    "override fun placeSubtreeOfNode(localRoot: yfiles.algorithms.Node, parentConnectorDirection: yfiles.tree.ParentConnectorDirection): yfiles.tree.RotatedSubtreeShape = definedExternally")

            baseClassName == "yfiles.tree.NodePlacerBase"
            -> lines("override fun determineChildConnector(child: yfiles.algorithms.Node): yfiles.tree.ParentConnectorDirection = definedExternally",
                    "override fun placeSubtreeOfNode(localRoot: yfiles.algorithms.Node, parentConnectorDirection: yfiles.tree.ParentConnectorDirection): yfiles.tree.SubtreeShape = definedExternally")

            baseClassName == "yfiles.layout.MultiStageLayout"
            -> "override fun applyLayoutCore(graph: yfiles.layout.LayoutGraph): Unit = definedExternally"

            baseClassName == "yfiles.view.ModelManager<T>"
            -> lines("override fun getCanvasObjectGroup(item: T): ICanvasObjectGroup = definedExternally",
                    "override fun getInstaller(item: T): ICanvasObjectInstaller = definedExternally",
                    "override fun onDisabled() = definedExternally",
                    "override fun onEnabled() = definedExternally")

            baseClassName == "yfiles.view.EdgeDecorationInstaller"
            -> lines("override fun getBendDrawing(canvas: CanvasComponent, edge: yfiles.graph.IEdge): IVisualTemplate = definedExternally",
                    "override fun getStroke(canvas: CanvasComponent, edge: yfiles.graph.IEdge): Stroke = definedExternally")

            className == "yfiles.view.ColorExtension"
            -> "override fun provideValue(serviceProvider: yfiles.graph.ILookup): $OBJECT_TYPE = definedExternally"

            className == "yfiles.graph.CompositeUndoUnit"
            -> lines("override fun tryMergeUnit(unit: IUndoUnit): Boolean = definedExternally",
                    "override fun tryReplaceUnit(unit: IUndoUnit): Boolean = definedExternally")

            className == "yfiles.graph.EdgePathLabelModel" || className == "yfiles.graph.EdgeSegmentLabelModel"
            -> lines("override fun findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter = definedExternally",
                    "override fun getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter> = definedExternally",
                    "override fun getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle = definedExternally")

            baseClassName == "yfiles.graph.FoldingLabelOwnerState"
            -> "override fun addLabel(text: String, layoutParameter: ILabelModelParameter, style: yfiles.styles.ILabelStyle, preferredSize: yfiles.geometry.Size, tag: $OBJECT_TYPE): FoldingLabelState = definedExternally"

            className == "yfiles.graph.FreeLabelModel"
            -> "override fun findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter = definedExternally"

            className == "yfiles.graph.GenericLabelModel"
            -> lines("override fun canConvert(context: yfiles.graphml.IWriteContext, value: $OBJECT_TYPE): Boolean = definedExternally",
                    "override fun getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter> = definedExternally",
                    "override fun convert(context: yfiles.graphml.IWriteContext, value: $OBJECT_TYPE): yfiles.graphml.MarkupExtension = definedExternally",
                    "override fun getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle = definedExternally")

            className == "yfiles.graphml.MapperOutputHandler"
            -> lines("override fun getValue(context: IWriteContext, key: TKey): TData = definedExternally",
                    "override fun writeValueCore(context: IWriteContext, data: TData) = definedExternally")

            className == "yfiles.graphml.MapperInputHandler"
            -> lines("override fun parseDataCore(context: IParseContext, node: org.w3c.dom.Node): TData = definedExternally",
                    "override fun setValue(context: IParseContext, key: TKey, data: TData) = definedExternally")

            className == "yfiles.graph.GenericPortLocationModel"
            -> lines("override fun canConvert(context: yfiles.graphml.IWriteContext, value: $OBJECT_TYPE): Boolean = definedExternally",
                    "override fun convert(context: yfiles.graphml.IWriteContext, value: $OBJECT_TYPE): yfiles.graphml.MarkupExtension = definedExternally",
                    "override fun getEnumerator(): yfiles.collections.IEnumerator<IPortLocationModelParameter> = definedExternally")

            baseClassName == "yfiles.layout.LayoutGraph"
            -> lines("override fun createLabelFactory(): ILabelLayoutFactory = definedExternally",
                    "override fun getLabelLayout(node: yfiles.algorithms.Node): Array<INodeLabelLayout> = definedExternally",
                    "override fun getLabelLayout(edge: yfiles.algorithms.Edge): Array<IEdgeLabelLayout> = definedExternally",
                    "override fun getLayout(node: yfiles.algorithms.Node): INodeLayout = definedExternally",
                    "override fun getLayout(edge: yfiles.algorithms.Edge): IEdgeLayout = definedExternally",
                    "override fun getOwnerEdge(labelLayout: IEdgeLabelLayout): yfiles.algorithms.Edge = definedExternally",
                    "override fun getOwnerNode(labelLayout: INodeLabelLayout): yfiles.algorithms.Node = definedExternally")

            className == "yfiles.hierarchic.PortCandidateOptimizer"
            -> "override fun optimizeAfterSequencingForSingleNode(node: yfiles.algorithms.Node, inEdgeOrder: yfiles.collections.IComparer<$OBJECT_TYPE>, outEdgeOrder: yfiles.collections.IComparer<$OBJECT_TYPE>, graph: yfiles.layout.LayoutGraph, ldp: ILayoutDataProvider, itemFactory: IItemFactory) = definedExternally"

            baseClassName != null && baseClassName.startsWith("yfiles.input.ConstrainedDragHandler<")
            -> "override fun constrainNewLocation(context: IInputModeContext, originalLocation: yfiles.geometry.Point, newLocation: yfiles.geometry.Point): yfiles.geometry.Point = definedExternally"

            className == "yfiles.input.PortRelocationHandleProvider"
            -> "override fun getHandle(context: IInputModeContext, edge: yfiles.graph.IEdge, sourceHandle: Boolean): IHandle = definedExternally"

            baseClassName != null && baseClassName.startsWith("yfiles.styles.PathBasedEdgeStyleRenderer<")
            -> lines("override fun createPath(): yfiles.geometry.GeneralPath = definedExternally",
                    "override fun getSourceArrow(): IArrow = definedExternally",
                    "override fun getStroke(): yfiles.view.Stroke = definedExternally",
                    "override fun getTargetArrow(): IArrow = definedExternally")

            className == "yfiles.styles.Arrow"
            -> lines("override val length: Number",
                    "    get() = definedExternally",
                    "override fun getBoundsProvider(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, directionVector: yfiles.geometry.Point): yfiles.view.IBoundsProvider = definedExternally",
                    "override fun getVisualCreator(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, direction: yfiles.geometry.Point): yfiles.view.IVisualCreator = definedExternally",
                    CLONE_OVERRIDE)

            className == "yfiles.styles.GraphOverviewSvgVisualCreator" || className == "yfiles.view.GraphOverviewCanvasVisualCreator"
            -> lines("override fun createVisual(context: yfiles.view.IRenderContext): yfiles.view.Visual = definedExternally",
                    "override fun updateVisual(context: yfiles.view.IRenderContext, oldVisual: yfiles.view.Visual): yfiles.view.Visual = definedExternally")

            className == "yfiles.view.DefaultPortCandidateDescriptor"
            -> lines("override fun createVisual(context: IRenderContext): Visual = definedExternally",
                    "override fun updateVisual(context: IRenderContext, oldVisual: Visual): Visual = definedExternally",
                    "override fun isInBox(context: yfiles.input.IInputModeContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                    "override fun isVisible(context: ICanvasContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                    "override fun getBounds(context: ICanvasContext): yfiles.geometry.Rect = definedExternally",
                    "override fun isHit(context: yfiles.input.IInputModeContext, location: yfiles.geometry.Point): Boolean = definedExternally")

            className == "yfiles.styles.VoidPathGeometry"
            -> lines("override fun getPath(): yfiles.geometry.GeneralPath = definedExternally",
                    "override fun getSegmentCount(): Number = definedExternally",
                    "override fun getTangent(ratio: Number): yfiles.geometry.Tangent = definedExternally",
                    "override fun getTangent(segmentIndex: Number, ratio: Number): yfiles.geometry.Tangent = definedExternally")

            else -> ""
        }
    }

    private fun lines(vararg lines: String): String {
        return lines.joinToString("\n")
    }

    private val PARAMETERS_CORRECTION = mapOf(
            ParameterData("yfiles.lang.IComparable", "compareTo", "obj") to "o",
            ParameterData("yfiles.lang.TimeSpan", "compareTo", "obj") to "o",
            ParameterData("yfiles.collections.IEnumerable", "includes", "value") to "item",

            ParameterData("yfiles.algorithms.YList", "elementAt", "i") to "index",
            ParameterData("yfiles.algorithms.YList", "includes", "o") to "item",
            ParameterData("yfiles.algorithms.YList", "indexOf", "obj") to "item",
            ParameterData("yfiles.algorithms.YList", "insert", "element") to "item",
            ParameterData("yfiles.algorithms.YList", "remove", "o") to "item",

            ParameterData("yfiles.graph.DefaultGraph", "setLabelPreferredSize", "size") to "preferredSize",

            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "createModelParameter", "sourceNode") to "sourceLayout",
            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "createModelParameter", "targetNode") to "targetLayout",
            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "label") to "labelLayout",
            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "sourceNode") to "sourceLayout",
            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "targetNode") to "targetLayout",
            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
            ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

            ParameterData("yfiles.layout.FreeEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
            ParameterData("yfiles.layout.FreeEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
            ParameterData("yfiles.layout.FreeEdgeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

            ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "createModelParameter", "sourceNode") to "sourceLayout",
            ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "createModelParameter", "targetNode") to "targetLayout",
            ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
            ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
            ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "getLabelPlacement", "para") to "parameter",

            ParameterData("yfiles.layout.INodeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",
            ParameterData("yfiles.layout.FreeNodeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

            ParameterData("yfiles.tree.NodeOrderComparer", "compare", "edge1") to "x",
            ParameterData("yfiles.tree.NodeOrderComparer", "compare", "edge2") to "y",

            ParameterData("yfiles.seriesparallel.DefaultOutEdgeComparer", "compare", "o1") to "x",
            ParameterData("yfiles.seriesparallel.DefaultOutEdgeComparer", "compare", "o2") to "y",

            ParameterData("yfiles.view.LinearGradient", "accept", "item") to "node",
            ParameterData("yfiles.view.RadialGradient", "accept", "item") to "node",

            ParameterData("yfiles.graphml.GraphMLParseValueSerializerContext", "lookup", "serviceType") to "type",
            ParameterData("yfiles.graphml.GraphMLWriteValueSerializerContext", "lookup", "serviceType") to "type",

            ParameterData("yfiles.layout.MultiStageLayout", "applyLayout", "layoutGraph") to "graph",

            ParameterData("yfiles.hierarchic.DefaultLayerSequencer", "sequenceNodeLayers", "glayers") to "layers",
            ParameterData("yfiles.input.ReparentStripeHandler", "reparent", "stripe") to "movedStripe",
            ParameterData("yfiles.multipage.IElementFactory", "createConnectorNode", "edgesIds") to "edgeIds",
            ParameterData("yfiles.router.DynamicObstacleDecomposition", "init", "partitionBounds") to "bounds",
            ParameterData("yfiles.view.StripeSelection", "isSelected", "stripe") to "item"
    )

    fun fixParameterName(className: String, functionName: String, parameterName: String): String {
        return PARAMETERS_CORRECTION[ParameterData(className, functionName, parameterName)] ?: parameterName
    }

    private data class ParameterData(val className: String, val functionName: String, val parameterName: String)
}

object MixinHacks {
    fun getImplementedTypes(className: String, implementedTypes: List<String>): List<String> {
        return when (className) {
            "yfiles.collections.Map" -> listOf("yfiles.collections.IMap<TKey, TValue>")
            "yfiles.geometry.IRectangle" -> existedItem("yfiles.geometry.IPoint", implementedTypes)
            "yfiles.geometry.IMutableRectangle" -> existedItem("yfiles.geometry.IRectangle", implementedTypes)
            "yfiles.geometry.MutableRectangle" -> existedItem("yfiles.geometry.IMutableRectangle", implementedTypes)
            "yfiles.geometry.IMutableOrientedRectangle" -> existedItem("yfiles.geometry.IOrientedRectangle", implementedTypes)
            "yfiles.geometry.OrientedRectangle" -> existedItem("yfiles.geometry.IMutableOrientedRectangle", implementedTypes)
            else -> implementedTypes
        }
    }

    private fun existedItem(item: String, items: List<String>): List<String> {
        if (items.contains(item)) {
            return listOf(item)
        }

        throw GradleException("Item '$item' not contains in item list '$items'")
    }

    val MUST_BE_ABSTRACT_CLASSES = listOf(
            "yfiles.collections.ICollection",
            "yfiles.collections.IList",
            "yfiles.collections.IMap",
            "yfiles.collections.IListEnumerable",
            "yfiles.collections.IObservableCollection",
            "yfiles.view.ICanvasObjectGroup",
            "yfiles.view.ISelectionModel",
            "yfiles.view.IStripeSelection",
            "yfiles.view.IGraphSelection",

            "yfiles.graph.IColumn",
            "yfiles.graph.IRow"
    )

    fun defineLikeAbstractClass(className: String, functions: List<Function>, properties: List<Property>): Boolean {
        if (className in MUST_BE_ABSTRACT_CLASSES) {
            return true
        }

        return functions.any { !it.abstract } || properties.any { !it.abstract }
    }
}