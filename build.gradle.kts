import Build_gradle.Hacks.getAdditionalContent
import Build_gradle.Hacks.isClassParameter
import Build_gradle.Hacks.validateStaticConstType
import Build_gradle.TypeParser.getGenericString
import Build_gradle.TypeParser.parseType
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

    val yfilesNamespace = apiRoot.namespaces.first { it.id == "yfiles" }
    for (namespace in yfilesNamespace.namespaces) {
        namespace.types.forEach { println(it.id) }
    }

    val types = yfilesNamespace.namespaces.flatMap { it.types }
    // types.removeIf { Hacks.redundantDeclaration(it) }

    ClassRegistry.instance = ClassRegistryImpl(types)

    val sourceDir = projectDir.resolve("generated/src/main/kotlin")

    val fileGenerator = FileGenerator(types)
    fileGenerator.generate(sourceDir)

    // TODO: Check if this class really needed
    sourceDir.resolve("yfiles/lang/Boolean.kt").delete()
    sourceDir.resolve("yfiles/lang/Number.kt").delete()
    sourceDir.resolve("yfiles/lang/String.kt").delete()
    sourceDir.resolve("yfiles/lang/Struct.kt").delete()
}

abstract class JsonWrapper(val source: JSONObject)
abstract class JDeclaration : JsonWrapper {
    companion object {
        fun code(vararg lines: String): String {
            return lines.joinToString("\n")
        }
    }

    val id: String by StringDelegate()
    val name: String by StringDelegate()
    val modifiers: JModifiers by ModifiersDelegate()

    val summary: String by StringDelegate()
    val remarks: String by StringDelegate()

    val fqn: String
    val nameOfClass: String

    val classRegistry: ClassRegistry
        get() = ClassRegistry.instance

    constructor(source: JSONObject) : super(source) {
        this.fqn = id
        this.nameOfClass = fqn.split(".").last()
    }

    constructor(fqn: String, source: JSONObject) : super(source) {
        this.fqn = fqn
        this.nameOfClass = fqn.split(".").last()
    }

    override fun toString(): String {
        return ""
        // TODO: uncomment
        // throw Gradle exception("toString() method must be overridden for object " + this)
    }
}

class JAPIRoot(source: JSONObject) : JsonWrapper(source) {
    val namespaces: List<JNamespace> by ArrayDelegate { JNamespace(it) }

    val x = 5.apply { }
}

class JNamespace(source: JSONObject) : JsonWrapper(source) {
    companion object {
        fun parseType(source: JSONObject): JType {
            val group = source.getString("group")
            return when (group) {
                "class" -> JClass(source)
                "interface" -> JInterface(source)
                "enum" -> JEnum(source)
                else -> throw GradleException("Undefined type group '$group'")
            }
        }
    }

    val id: String by StringDelegate()
    val name: String by StringDelegate()

    val namespaces: List<JNamespace> by ArrayDelegate { JNamespace(it) }
    val types: List<JType> by ArrayDelegate { parseType(it) }
}

abstract class JType(source: JSONObject) : JDeclaration(source) {
    val fields: List<JField> by ArrayDelegate { JField(this.fqn, it) }
    val properties: List<JProperty> by ArrayDelegate { JProperty(this.fqn, it) }
    val methods: List<JMethod> by ArrayDelegate { JMethod(this.fqn, it) }
    val staticMethods: List<JMethod> by ArrayDelegate { JMethod(this.fqn, it) }

    val typeparameters: List<JTypeParameter> by ArrayDelegate { JTypeParameter(it) }

    val extends: String? by NullableStringDelegate()
    val implements: List<String> by StringArrayDelegate()

    fun genericParameters(): String {
        return getGenericString(typeparameters)
    }

    fun extendedType(): String? {
        if (Hacks.ignoreExtendedType(fqn)) {
            return null
        }

        val type = extends ?: return null
        return parseType(type)
    }

    fun implementedTypes(): List<String> {
        var types = Hacks.getImplementedTypes(fqn)
        if (types != null) {
            return types
        }

        types = implements.map { parseType(it) }
        return MixinHacks.getImplementedTypes(fqn, types)
    }
}

class JClass(source: JSONObject) : JType(source) {
    val static = modifiers.static
    val final = modifiers.final
    val open = !final
    val abstract = modifiers.abstract

    val modificator = when {
        abstract -> "abstract" // no such cases (JS specific?)
        open -> "open"
        else -> ""
    }

    val constructors: List<JConstructor> by ArrayDelegate { JConstructor(this.fqn, it) }
}

class JModifiers(flags: List<String>) {
    val static = flags.contains("static")
    val final = flags.contains("final")
    val readOnly = flags.contains("ro")
    val abstract = flags.contains("abstract")
    val protected = flags.contains("protected")
}

class JInterface(source: JSONObject) : JType(source)

class JEnum(source: JSONObject) : JType(source) {
    val constructors: List<JConstructor> by ArrayDelegate { JConstructor(this.fqn, it) }
}

abstract class JTypedDeclaration(fqn: String, source: JSONObject) : JDeclaration(fqn, source) {
    val type: String by TypeDelegate { TypeParser.parse(it) }
}

class JConstructor(fqn: String, source: JSONObject) : JMethodBase(fqn, source) {
    val adapter: JConstructor? = null // TODO: implement
    val protected = modifiers.protected

    val modificator: String = when {
        protected -> "protected "
        else -> ""
    }

    override fun toString(): String {
        if (generated) {
            val generic = Hacks.getGenerics(fqn)
            return code(
                    "fun $generic $name.Companion.create(${parametersString()}): $name$generic {",
                    "    return $name(${mapString(parameters)})",
                    "}"
            )
        }

        return "${modificator}constructor(${parametersString()})"
    }
}

class JField(fqn: String, source: JSONObject) : JTypedDeclaration(fqn, source) {
    val static = modifiers.static
}

class JProperty(fqn: String, source: JSONObject) : JTypedDeclaration(fqn, source) {
    val static = modifiers.static
    val getterSetter = !modifiers.readOnly

    val abstract = modifiers.abstract
}

class JMethod(fqn: String, source: JSONObject) : JMethodBase(fqn, source) {
    val adapter: JMethod? = null // TODO: implement

    val abstract = modifiers.abstract
    val static = modifiers.static
    val protected = modifiers.protected

    val typeparameters: List<JTypeParameter> by ArrayDelegate { JTypeParameter(it) }
    val returns: JReturns? by ReturnsDelegate()

    val generics: String
        get() = Hacks.getFunctionGenerics(fqn, name) ?: getGenericString(typeparameters)

    private fun modificator(canBeOpen: Boolean = true): String {
        // TODO: add abstract modificator if needed
        if (!generated && classRegistry.functionOverriden(fqn, name)) {
            return "override "
        }

        val result = when {
            abstract -> "abstract "
            canBeOpen && !static && !classRegistry.isFinalClass(fqn) -> "open "
            else -> ""
        }
        return result + when {
            protected -> "protected "
            else -> ""
        }
    }

    override fun toString(): String {
        val returnType = returns?.type ?: UNIT

        if (generated) {
            val instanceName = nameOfClass + if (static) ".Companion" else ""
            return code(
                    "${modificator()}fun $generics $instanceName.$name(${parametersString()}): $returnType {",
                    "    return $name(${mapString(parameters)})",
                    "}"
            )
        }

        val body = if (abstract) "" else " = definedExternally"
        return "${modificator()}fun $generics$name(${parametersString()}): $returnType$body"
    }
}

abstract class JMethodBase(fqn: String, source: JSONObject) : JDeclaration(fqn, source) {
    val parameters: List<JParameter> by ArrayDelegate { JParameter(it) }

    val generated = false // TODO: realize

    protected fun mapString(parameters: List<JParameter>): String {
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
                    if (it.optional) {
                        // TODO: fix compilation hack
                        val defaultValue = it.defaultValue
                        if (defaultValue == "null") {
                            str += "?"
                        }
                        if (useDefaultValue) {
                            str += when {
                                classRegistry.functionOverriden(fqn, name) -> ""
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

    override fun hashCode(): Int {
        return Objects.hash(fqn, name, parametersString(false))
    }

    override fun equals(other: Any?): Boolean {
        return other is JMethodBase
                && Objects.equals(fqn, other.fqn)
                && Objects.equals(name, other.name)
                && Objects.equals(parametersString(false), other.parametersString(false))
    }
}

class JParameter(source: JSONObject) : JsonWrapper(source) {
    companion object {
        fun create(name: String, type: String): JParameter {
            TODO()
        }

        fun create(name: String, type: String, defaultValue: String?, vararg: Boolean): JParameter {
            TODO()
        }
    }

    val name: String by StringDelegate()
    val type: String by TypeDelegate { TypeParser.parse(it) }
    val vararg: Boolean = false // TODO: add reading
    val summary: String by StringDelegate()
    val optional: Boolean by BooleanDelegate()

    val defaultValue: String = "" // TODO: add reading
}

class JTypeParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
}

class JReturns(val type: String, source: JSONObject) : JsonWrapper(source)

class ArrayDelegate<T>(private val transform: (JSONObject) -> T) {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): List<T> {
        val source = thisRef.source
        val key = property.name

        if (!source.has(key)) {
            return emptyList()
        }

        val array = source.getJSONArray(key)
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
    companion object {
        fun value(thisRef: JsonWrapper, property: KProperty<*>): List<String> {
            val source = thisRef.source
            val key = property.name

            if (!source.has(key)) {
                return emptyList()
            }

            val array = source.getJSONArray(key)
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

    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): List<String> {
        return value(thisRef, property)
    }
}

class NullableStringDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String? {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) source.getString(key) else null
    }
}

class StringDelegate {
    companion object {
        fun value(thisRef: JsonWrapper, property: KProperty<*>): String {
            return thisRef.source.getString(property.name)
        }
    }

    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String {
        return value(thisRef, property)
    }
}

class BooleanDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): Boolean {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) source.getBoolean(key) else false
    }
}

class TypeDelegate(private val parse: (String) -> String) {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String {
        return parse(StringDelegate.value(thisRef, property))
    }
}

class ModifiersDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): JModifiers {
        return JModifiers(StringArrayDelegate.value(thisRef, property))
    }
}

class ReturnsDelegate {
    operator fun getValue(thisRef: JMethod, property: KProperty<*>): JReturns? {
        val source = thisRef.source
        val key = property.name

        val hackedType = Hacks.getReturnType(thisRef.fqn, thisRef.name)
        if (hackedType != null) {
            return JReturns(hackedType, source)
        }

        return if (source.has(key)) {
            val data = source.getJSONObject(key)
            val type = TypeParser.parse(data.getString("type"))
            JReturns(type, data)
        } else {
            null
        }
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

object TypeParser {
    private val FUNCTION_START = "function("
    private val FUNCTION_END = "):"
    private val FUNCTION_END_VOID = ")"

    private val GENERIC_START = "<"
    private val GENERIC_END = ">"

    private val STANDARD_TYPE_MAP = mapOf(
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

    fun parse(type: String): String {
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

    fun getGenericString(parameters: List<JTypeParameter>): String {
        return if (parameters.isEmpty()) "" else "<${parameters.map { it.name }.joinToString(", ")}> "
    }

    fun parseParamLine(line: String, function: String): JParameter {
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
        return JParameter.create(name, type, defaultValue, vararg)
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
                    parameters.indexOf("<", index),
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
        val voidResult = type.endsWith(FUNCTION_END_VOID)
        val functionEnd = if (voidResult) FUNCTION_END_VOID else FUNCTION_END
        val parameterTypes = StringUtil.between(type, FUNCTION_START, functionEnd)
                .split(",").map({ parseType(it) })
        val resultType = if (voidResult) UNIT else parseType(StringUtil.from(type, FUNCTION_END))
        return "(${parameterTypes.joinToString(", ")}) -> $resultType"
    }
}

interface ClassRegistry {
    companion object {
        private var _instance: ClassRegistry? = null

        var instance: ClassRegistry
            get() {
                return _instance ?: throw GradleException("ClassRegistry instance not initialized!")
            }
            set(value) {
                _instance = value
            }
    }

    fun isInterface(className: String): Boolean
    fun isFinalClass(className: String): Boolean
    fun isGetterSetter(className: String, propertyName: String): Boolean
    fun functionOverriden(className: String, functionName: String): Boolean
    fun propertyOverriden(className: String, functionName: String): Boolean
}

/*
class Const(lines: List<String>) {
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

class Property(lines: List<String>) {
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

open class Function {
    companion object {
        val START = "function("
        val END = "){};"

        private fun calculateParameters(lines: List<String>): Parameters {
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

    protected constructor(lines: List<String>, parameters: Parameters, generated: Boolean) : super() {
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

    constructor(lines: List<String>) : this(lines, calculateParameters(lines), false)

    open fun generateAdapter(parameters: List<Parameter>): Function {
        return Function(lines, Parameters(parameters), true)
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

class EnumValue(private val lines: List<String>)

class Parameters(val items: List<Parameter>, val generatedItems: List<Parameter>? = null)

data class Parameter(val name: String, val type: String, val defaultValue: String? = null, val vararg: Boolean = false)
*/

class ClassRegistryImpl(types: List<JType>) : ClassRegistry {
    private val instances = types.associateBy({ it.fqn }, { it })

    private val functionsMap = types.associateBy(
            { it.fqn },
            { it.methods.map { it.name } }
    )

    private val propertiesMap = types.associateBy(
            { it.fqn },
            { it.properties.map { it.name } }
    )

    private val propertiesMap2 = types.associateBy(
            { it.fqn },
            { it.properties.associateBy({ it.name }, { it.getterSetter }) }
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
        return instances[className] is JInterface
    }

    override fun isFinalClass(className: String): Boolean {
        val instance = instances[className]
        return instance is JClass && instance.final
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

class FileGenerator(private val types: List<JType>) {
    fun generate(directory: File) {
        directory.mkdirs()
        directory.deleteRecursively()

        types.forEach {
            val generatedFile = when (it) {
                is JClass -> ClassFile(it)
                is JInterface -> InterfaceFile(it)
                is JEnum -> EnumFile(it)
                else -> throw GradleException("Undefined type for generation: " + it)
            }

            generate(directory, generatedFile)
        }
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

    abstract class GeneratedFile(private val declaration: JType) {
        val className = declaration.fqn
        val fqn: FQN = FQN(className)

        val consts: List<JField>
            get() = declaration.fields
                    .sortedBy { it.name }

        val functions: List<JMethod>
            get() = declaration.methods
                    .sortedBy { it.name }

        val properties: List<JProperty>
            get() = declaration.properties
                    .sortedBy { it.name }

        val staticConsts: List<JField>
            get() = consts.filter { it.static }

        val staticProperties: List<JProperty>
            get() = properties.filter { it.static }

        val staticFunctions: List<JMethod>
            get() = functions.filter { it.static }

        val staticDeclarations: List<JDeclaration>
            get() {
                return mutableListOf<JDeclaration>()
                        .union(staticConsts)
                        .union(staticProperties)
                        .union(staticFunctions.toSet())
                        .toList()
            }

        val memberConsts: List<JField>
            get() = consts.filter { !it.static }

        val memberProperties: List<JProperty>
            get() = properties.filter { !it.static }

        val memberFunctions: List<JMethod>
            get() = functions.filter { !it.static }

        val header: String
            get() = "package ${fqn.packageName}\n"

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
                    else -> "    companion object {} \n\n"
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
            return listOf<JDeclaration>()
                    .union(memberConsts)
                    .union(memberProperties)
                    .union(memberFunctions)
                    .union(listOf(getAdditionalContent(declaration.fqn, declaration.extendedType())))
                    .joinToString("\n") + "\n"
        }
    }

    class ClassFile(private val declaration: JClass) : GeneratedFile(declaration) {
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
            val constructorSet = declaration.constructors.toSet()
            return constructorSet.map {
                it.toString()
            }.joinToString("\n") + "\n"
        }

        private fun adapters(): String {
            val constructorAdapters = Hacks.filterConstructorAdapters(
                    className,
                    declaration.constructors.mapNotNull { it.adapter }
            )

            val staticFunctionAdapters = declaration.methods
                    .filter { it.static }
                    .mapNotNull { it.adapter }
                    .filterNot { staticFunctions.contains(it) }

            val adapters = mutableListOf<JDeclaration>()
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

    class InterfaceFile(declaration: JInterface) : GeneratedFile(declaration) {
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

    class EnumFile(private val declaration: JEnum) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = declaration.fields
                    .map { "    val ${it.name}: ${it.nameOfClass} = definedExternally" }
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

    fun redundantMethod(method: JMethod): Boolean {
        if (method.fqn == OBJECT_TYPE) {
            return method.name in SYSTEM_FUNCTIONS
        }

        return false
    }

    fun parseParamLine(line: String, function: String): JParameter? {
        if (line.startsWith("@param value")) {
            return when (function) {
                "yfiles.collections.IList.prototype.set" -> JParameter.create("value", "T")
                "yfiles.collections.IMapper.prototype.set" -> JParameter.create("value", "V")
                "yfiles.graphml.CreationProperties.prototype.set" -> JParameter.create("value", OBJECT_TYPE)
                "yfiles.algorithms.YList.prototype.set" -> JParameter.create("value", OBJECT_TYPE)
                "yfiles.collections.IMap.prototype.set" -> JParameter.create("value", "TValue")
                else -> throw GradleException("No hacked parameter value for function $function")
            }
        }

        if (line.startsWith("@param options.")) {
            val name = line.split(" ")[1]

            return when {
                function == "yfiles.collections.Map" && name == "options.entries" -> JParameter.create(name, "Array<MapEntry<TKey, TValue>>")
                function == "yfiles.collections.List" && name == "options.items" -> JParameter.create(name, "Array<T>")
                function == "yfiles.view.LinearGradient" || function == "yfiles.view.RadialGradient"
                        && name == "options.gradientStops" -> JParameter.create(name, "Array<yfiles.view.GradientStop>")
                function == "yfiles.graph.IGraph.prototype.createGroupNode" && name == "options.children" -> JParameter.create(name, "Array<$NODE_TYPE>")
                function == "yfiles.graph.ITable.prototype.createChildRow" && name == "options.childRows" -> JParameter.create(name, "Array<$ROW_TYPE>")
                function == "yfiles.graph.ITable.prototype.createChildColumn" && name == "options.childColumns" -> JParameter.create(name, "Array<$COLUMN_TYPE>")
                name == "options.nodes" -> JParameter.create(name, NODE_TYPE)
                name == "options.edges" -> JParameter.create(name, EDGE_TYPE)
                name == "options.ports" -> JParameter.create(name, PORT_TYPE)
                name == "options.labels" -> JParameter.create(name, LABEL_TYPE)
                name == "options.bends" -> JParameter.create(name, BEND_TYPE)
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

    fun filterConstructorAdapters(className: String, adapters: List<JConstructor>): List<JConstructor> {
        return when (className) {
            "yfiles.styles.NodeStylePortStyleAdapter" -> adapters.toSet().toList()
            else -> adapters
        }
    }

    fun getReturnType(className: String, name: String): String? {
        return when {
            className == "yfiles.algorithms.EdgeList" && name == "getEnumerator" -> "yfiles.collections.IEnumerator<yfiles.lang.Object>"
            className == "yfiles.algorithms.NodeList" && name == "getEnumerator" -> "yfiles.collections.IEnumerator<yfiles.lang.Object>"

            className == "yfiles.input.ConstrainedReshapeHandler" && name == "handleReshape" -> UNIT
            className == "yfiles.input.ConstrainedDragHandler" && name == "handleMove" -> UNIT
            className == "yfiles.geometry.MutableSize" && name == "MutableSize" -> ""
        // TODO: check (in official doc no return type)
            className == "yfiles.geometry.OrientedRectangle" && name == "moveBy" -> "Boolean"
            else -> null
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

    fun defineLikeAbstractClass(className: String, functions: List<JMethod>, properties: List<JProperty>): Boolean {
        if (className in MUST_BE_ABSTRACT_CLASSES) {
            return true
        }

        return functions.any { !it.abstract } || properties.any { !it.abstract }
    }
}