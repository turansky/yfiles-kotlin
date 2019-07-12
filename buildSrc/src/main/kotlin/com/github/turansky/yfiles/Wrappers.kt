package com.github.turansky.yfiles

import org.json.JSONObject
import kotlin.reflect.KProperty

internal abstract class JsonWrapper(val source: JSONObject) {
    open fun toCode(): String {
        throw IllegalStateException("toCode() method must be overridden")
    }

    final override fun toString(): String {
        throw IllegalStateException("Use method toCode() instead")
    }
}

internal abstract class Declaration : JsonWrapper {
    val id: String by StringDelegate()
    val name: String by StringDelegate()
    protected val modifiers: Modifiers by ModifiersDelegate()

    val summary: String by StringDelegate()
    val remarks: String by StringDelegate()

    val fqn: ClassId

    constructor(source: JSONObject) : super(source) {
        this.fqn = fixPackage(id)
    }

    constructor(fqn: ClassId, source: JSONObject) : super(source) {
        this.fqn = fixPackage(fqn)
    }
}

internal class ApiRoot(source: JSONObject) : JsonWrapper(source) {
    private val namespaces: List<Namespace> by ArrayDelegate(::Namespace)
    val types: List<Type>
        get() = namespaces
            .asSequence()
            .flatMap { it.namespaces.asSequence() }
            .flatMap { it.types.asSequence() }
            .toList()

    val functionSignatures: Map<String, FunctionSignature> by MapDelegate { name, source -> FunctionSignature(name, source) }
}

private class Namespace(source: JSONObject) : JsonWrapper(source) {
    companion object {
        fun parseType(source: JSONObject): Type {
            val group = source.getString("group")
            return when (group) {
                "class" -> Class(source)
                "interface" -> Interface(source)
                "enum" -> Enum(source)
                else -> throw IllegalArgumentException("Undefined type group '$group'")
            }
        }
    }

    val name: String by StringDelegate()

    val namespaces: List<Namespace> by ArrayDelegate { Namespace(it) }
    val types: List<Type> by ArrayDelegate { parseType(it) }
}

internal class FunctionSignature(fqn: ClassId, source: JSONObject) : JsonWrapper(source) {
    val fqn = fixPackage(fqn)
    val summary: String by StringDelegate()
    private val parameters: List<SignatureParameter> by ArrayDelegate(::SignatureParameter)
    private val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)
    private val returns: SignatureReturns? by SignatureReturnsDelegate()

    override fun toCode(): String {
        val typeparameters = typeparameters
        val generics = if (typeparameters.isNotEmpty()) {
            "<${typeparameters.byComma { it.name }}>"
        } else {
            ""
        }
        val parameters = parameters
            .byComma { it.toCode() }
        val returns = returns?.toCode() ?: UNIT

        val data = GeneratorData(fqn)
        return "typealias ${data.name}$generics = ($parameters) -> $returns"
    }
}

internal class SignatureParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    val type: String by TypeDelegate { parseType(it) }
    val summary: String by StringDelegate()

    // TODO: remove temp nullability fix
    override fun toCode(): String {
        return "$name: $type" + exp(type == ANY, "?")
    }
}

internal class SignatureReturns(source: JSONObject) : JsonWrapper(source) {
    private val type: String by TypeDelegate { parseType(it) }
    private val doc: String? by NullableStringDelegate()

    override fun toCode(): String {
        // TODO: remove specific hack for MapperDelegate
        return if (type == "V" || doc?.contains("or <code>null</code>") == true) {
            "$type?"
        } else {
            type
        }
    }
}

internal sealed class Type(source: JSONObject) : Declaration(source) {
    val es6name: String? by NullableStringDelegate()

    val constants: List<Constant> by ArrayDelegate { Constant(fqn, it) }

    val properties: List<Property> by ArrayDelegate { Property(fqn, it) }
    val staticProperties: List<Property> by ArrayDelegate { Property(fqn, it) }

    val methods: List<Method> by ArrayDelegate { Method(fqn, it) }
    val staticMethods: List<Method> by ArrayDelegate { Method(fqn, it) }

    val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)

    private val extends: String? by NullableStringDelegate()
    private val implements: List<String> by StringArrayDelegate()

    fun genericParameters(): String {
        return getGenericString(typeparameters)
    }

    fun extendedType(): String? {
        val type = extends ?: return null
        return parseType(type)
    }

    fun implementedTypes(): List<String> {
        return implements.map { parseType(it) }
    }
}

internal sealed class ExtendedType(source: JSONObject) : Type(source) {
    val events: List<Event> by ArrayDelegate { Event(fqn, it) }
}

internal class Class(source: JSONObject) : ExtendedType(source) {
    val final = modifiers.final
    val open = !final
    val abstract = modifiers.abstract

    val kotlinModificator = when {
        abstract -> "abstract"
        open -> "open"
        else -> ""
    }

    val constructors: List<Constructor> by ArrayDelegate { Constructor(this.fqn, it) }
}

internal class Modifiers(flags: List<String>) {
    val static = flags.contains(STATIC)
    val final = flags.contains(FINAL)
    val readOnly = flags.contains(RO)
    val abstract = flags.contains(ABSTRACT)
    val protected = flags.contains(PROTECTED)

    private val canbenull = flags.contains(CANBENULL)
    val nullability = if (canbenull) "?" else ""
}

internal class Interface(source: JSONObject) : ExtendedType(source)
internal class Enum(source: JSONObject) : Type(source)

internal abstract class TypedDeclaration(fqn: ClassId, source: JSONObject) : Declaration(fqn, source) {
    private val signature: String? by NullableStringDelegate()
    protected val type: String by TypeDelegate { parse(it, signature) }
}

internal class Constructor(fqn: ClassId, source: JSONObject) : MethodBase(fqn, source) {
    val protected = modifiers.protected

    override val overridden: Boolean = false

    override fun toCode(): String {
        val modificator: String = when {
            protected -> "protected"
            else -> ""
        }

        return "${modificator} constructor(${kotlinParametersString()})"
    }
}

internal class Constant(fqn: ClassId, source: JSONObject) : TypedDeclaration(fqn, source) {
    override fun toCode(): String {
        return "val $name: $type"
    }
}

internal class Property(fqn: ClassId, source: JSONObject) : TypedDeclaration(fqn, source) {
    val static = modifiers.static
    val protected = modifiers.protected
    val getterSetter = !modifiers.readOnly

    val abstract = modifiers.abstract
    val final = modifiers.final
    val open = !static && !final

    private val overridden: Boolean
        get() = !static && ClassRegistry.instance.propertyOverriden(fqn, name)

    override fun toCode(): String {
        var str = ""

        if (overridden) {
            str += "override " + exp(final, "final ")
        } else {
            if (protected) {
                str += "protected "
            }

            str += when {
                abstract -> "abstract "
                final -> "final "
                open -> "open "
                else -> ""
            }
        }

        str += if (getterSetter) "var " else "val "

        str += "$name: $type${modifiers.nullability}"
        return str
    }

    fun toExtensionCode(
        classDeclaration: String,
        typeparameters: List<TypeParameter>
    ): String {
        require(!protected)

        val generics = getGenericString(typeparameters)

        var str = "inline " + if (getterSetter) "var " else "val "
        str += "$generics ${classDeclaration}.$name: $type${modifiers.nullability}\n" +
                "    get() = $AS_DYNAMIC.$name"
        if (getterSetter) {
            str += "\n    set(value) { $AS_DYNAMIC.$name = value }"
        }

        return str
    }
}

private val OPERATOR_NAMES = setOf(
    "get",
    "contains"
)

internal class Method(fqn: ClassId, source: JSONObject) : MethodBase(fqn, source) {
    val abstract = modifiers.abstract
    val static = modifiers.static
    val protected = modifiers.protected

    val final = modifiers.final
    val open = !static && !final

    val isExtension by BooleanDelegate()

    val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)
    val returns: Returns? by ReturnsDelegate()

    val generics: String
        get() = getGenericString(typeparameters)

    override val overridden: Boolean
        get() = !static && ClassRegistry.instance.functionOverriden(fqn, name)

    private fun kotlinModificator(): String {
        if (isExtension) {
            require(!protected)
            require(!abstract)
            return ""
        }

        if (overridden) {
            return "override " + exp(final, "final ")
        }

        return when {
            abstract -> "abstract "
            final -> "final "
            open -> "open "
            else -> ""
        } + exp(protected, "protected ")
    }

    // https://youtrack.jetbrains.com/issue/KT-31249
    private fun getReturnSignature(): String =
        returns?.let {
            ":" + it.type + modifiers.nullability
        } ?: ""

    override fun toCode(): String {
        val operator = exp(
            name in OPERATOR_NAMES && parameters.size == 1,
            " operator "
        )

        return "${kotlinModificator()} $operator fun $generics$name(${kotlinParametersString()})${getReturnSignature()}"
    }

    fun toExtensionCode(
        classDeclaration: String,
        typeparameters: List<TypeParameter>
    ): String {
        require(!protected)

        val extParameters = kotlinParametersString(extensionMode = true)
        val callParameters = parameters
            .byComma { it.name }

        val returnSignature = getReturnSignature()

        val generics = getGenericString(typeparameters + this.typeparameters)

        return "inline fun $generics ${classDeclaration}.$name($extParameters)$returnSignature =\n" +
                "$AS_DYNAMIC.$name($callParameters)"
    }
}

// TODO: support artificial parameters
internal abstract class MethodBase(fqn: ClassId, source: JSONObject) : Declaration(fqn, source) {
    val parameters: List<Parameter> by ArrayDelegate(::Parameter)
    val options: Boolean by BooleanDelegate()

    protected abstract val overridden: Boolean

    protected fun kotlinParametersString(
        extensionMode: Boolean = false
    ): String {
        return parameters
            .byCommaLine {
                val modifiers = exp(extensionMode && it.lambda, "noinline ") +
                        exp(it.modifiers.vararg, "vararg ")

                val body = if (it.modifiers.optional && !overridden) {
                    if (extensionMode) EQ_NULL else EQ_DE
                } else {
                    ""
                }

                "$modifiers ${it.name}: ${it.type}${it.modifiers.nullability}" + body
            }
    }
}

internal class ParameterModifiers(flags: List<String>) {
    val artificial = flags.contains(ARTIFICIAL)
    val vararg = flags.contains(VARARGS)
    val optional = flags.contains(OPTIONAL)
    val conversion = flags.contains(CONVERSION)

    private val canbenull = flags.contains(CANBENULL)
    val nullability = if (canbenull) "?" else ""
}

internal class Parameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    private val signature: String? by NullableStringDelegate()
    val lambda: Boolean = signature != null
    val type: String by TypeDelegate { parse(it, signature) }
    val summary: String? by NullableStringDelegate()
    val modifiers: ParameterModifiers by ParameterModifiersDelegate()
}

internal class TypeParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
}

internal class Returns(source: JSONObject) : JsonWrapper(source) {
    private val signature: String? by NullableStringDelegate()
    val type: String by TypeDelegate { parse(it, signature) }
}

internal class Event(fqn: ClassId, source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    val summary: String by StringDelegate()
    private val add: EventListener by EventListenerDelegate(fqn)
    private val remove: EventListener by EventListenerDelegate(fqn)
    private val listeners = listOf(add, remove)

    val overriden by lazy {
        listeners.any { it.overriden }
    }

    val listenerNames: List<String>
        get() = listeners.map { it.name }

    override fun toCode(): String {
        return listeners
            .lines { it.toCode() }
    }

    fun toExtensionCode(
        classDeclaration: String,
        typeparameters: List<TypeParameter>
    ): String {
        val generics = getGenericString(typeparameters)
        val extensionName = "add${name}Handler"

        val listenerType = add.parameters.single().type
        val data = getHandlerData(listenerType)

        return """
                inline fun $generics ${classDeclaration}.$extensionName(
                crossinline handler: ${data.handlerType}
                ): () -> Unit {
                val listener: $listenerType = ${data.listenerBody}
                ${add.name}(listener)
                return { ${remove.name}(listener) }
                }
        """.trimIndent()
    }
}

private class EventListener(private val fqn: ClassId, source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    val modifiers: EventListenerModifiers by EventListenerModifiersDelegate()
    val parameters: List<Parameter> by ArrayDelegate(::Parameter)

    val overriden by lazy {
        ClassRegistry.instance
            .listenerOverriden(fqn, name)
    }

    private fun kotlinModificator(): String {
        return when {
            overriden -> "override "
            modifiers.abstract -> "abstract "
            else -> ""
        }
    }

    override fun toCode(): String {
        val parametersString = parameters
            .byComma { "${it.name}: ${it.type}" }

        return "${kotlinModificator()}fun $name($parametersString)"
    }
}

internal class EventListenerModifiers(flags: List<String>) {
    val public = flags.contains(PUBLIC)
    val abstract = flags.contains(ABSTRACT)
}

private class ArrayDelegate<T>(private val transform: (JSONObject) -> T) {
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

        return (0 until length)
            .asSequence()
            .map { array.getJSONObject(it) }
            .map(transform)
            .toList()
    }
}

private class StringArrayDelegate {
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

private class MapDelegate<T>(private val transform: (name: String, source: JSONObject) -> T) {

    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): Map<String, T> {
        val source = thisRef.source
        val key = property.name

        if (!source.has(key)) {
            return emptyMap()
        }

        val data = source.getJSONObject(key)
        val keys: List<String> = data.keySet()?.toList() ?: emptyList<String>()
        if (keys.isEmpty()) {
            return emptyMap()
        }

        return keys.associateBy({ it }, { transform(it, data.getJSONObject(it)) })
    }
}

private class NullableStringDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String? {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) source.getString(key) else null
    }
}

private class StringDelegate {
    companion object {
        fun value(thisRef: JsonWrapper, property: KProperty<*>): String {
            return thisRef.source.getString(property.name)
        }
    }

    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String {
        return value(thisRef, property)
    }
}

private class BooleanDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): Boolean {
        val source = thisRef.source
        val key = property.name

        if (!source.has(key)) {
            return false
        }

        val value = source.getString(key)
        return when (value) {
            "!0" -> true
            "!1" -> false
            else -> source.getBoolean(key)
        }
    }
}

private class TypeDelegate(private val parse: (String) -> String) {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String {
        return parse(StringDelegate.value(thisRef, property))
    }
}

private class ModifiersDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): Modifiers {
        return Modifiers(StringArrayDelegate.value(thisRef, property))
    }
}

private class ParameterModifiersDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): ParameterModifiers {
        return ParameterModifiers(StringArrayDelegate.value(thisRef, property))
    }
}

private class SignatureReturnsDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): SignatureReturns? {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) {
            SignatureReturns(source.getJSONObject(key))
        } else {
            null
        }
    }
}

private class ReturnsDelegate {
    operator fun getValue(thisRef: Method, property: KProperty<*>): Returns? {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) {
            Returns(source.getJSONObject(key))
        } else {
            null
        }
    }
}

private class EventListenerDelegate(private val fqn: ClassId) {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): EventListener {
        return EventListener(fqn, thisRef.source.getJSONObject(property.name))
    }
}

private class EventListenerModifiersDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): EventListenerModifiers {
        return EventListenerModifiers(StringArrayDelegate.value(thisRef, property))
    }
}

