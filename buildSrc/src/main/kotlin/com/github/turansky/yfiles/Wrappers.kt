package com.github.turansky.yfiles

import com.github.turansky.yfiles.json.*
import org.json.JSONObject
import kotlin.reflect.KProperty

internal abstract class JsonWrapper(override val source: JSONObject) : HasSource {
    open fun toCode(): String {
        throw IllegalStateException("toCode() method must be overridden")
    }

    open fun toExtensionCode(): String {
        throw IllegalStateException("toExtensionCode() method must be overridden")
    }

    final override fun toString(): String {
        throw IllegalStateException("Use method toCode() instead")
    }
}

internal abstract class Declaration(source: JSONObject) : JsonWrapper(source), Comparable<Declaration> {
    val name: String by StringDelegate()
    protected val modifiers: Modifiers by ModifiersDelegate()

    protected val summary: String? by SummaryDelegate()
    protected val remarks: String by StringDelegate()
    protected val seeAlso: List<SeeAlso> by ArrayDelegate(::parseSeeAlso)

    override fun compareTo(other: Declaration): Int =
        name.compareTo(other.name)
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

internal class FunctionSignature(fqn: ClassId, source: JSONObject) : JsonWrapper(source), HasClassId {
    override val classId = fixPackage(fqn)

    private val summary: String? by SummaryDelegate()
    private val seeAlso: List<SeeAlso> by ArrayDelegate(::parseSeeAlso)

    private val parameters: List<SignatureParameter> by ArrayDelegate(::SignatureParameter)
    private val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)
    private val returns: SignatureReturns? by SignatureReturnsDelegate()

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            parameters = parameters,
            typeparameters = typeparameters,
            returns = returns,
            seeAlso = seeAlso
        )

    override fun toCode(): String {
        val generics = if (typeparameters.isNotEmpty()) {
            "<${typeparameters.byComma { it.name }}>"
        } else {
            ""
        }
        val parameters = parameters
            .byComma { it.toCode() }
        val returns = returns?.toCode() ?: UNIT

        val data = GeneratorData(classId)
        return documentation +
                "typealias ${data.name}$generics = ($parameters) -> $returns"
    }
}

internal interface IParameter {
    val name: String
    val summary: String?
}

internal class SignatureParameter(source: JSONObject) : JsonWrapper(source), IParameter {
    override val name: String by StringDelegate()
    val type: String by TypeDelegate { parseType(it) }
    override val summary: String? by SummaryDelegate()

    // TODO: remove temp nullability fix
    override fun toCode(): String {
        return "$name: $type" + exp(type == ANY, "?")
    }
}

internal interface IReturns {
    val doc: String?
}

internal class SignatureReturns(source: JSONObject) : JsonWrapper(source), IReturns {
    private val type: String by TypeDelegate { parseType(it) }
    override val doc: String? by SummaryDelegate()

    override fun toCode(): String {
        // TODO: remove specific hack for MapperDelegate
        return if (type == "V" || doc?.contains("or `null`") == true) {
            "$type?"
        } else {
            type
        }
    }
}

internal interface TypeDeclaration : HasClassId {
    val classDeclaration: String
    val typeparameters: List<TypeParameter>
}

internal sealed class Type(source: JSONObject) : Declaration(source), TypeDeclaration {
    private val id: String by StringDelegate()
    override val classId: ClassId = fixPackage(id)

    val es6name: String? by NullableStringDelegate()

    val constants: List<Constant> by ArrayDelegate { Constant(it) }

    val properties: List<Property> by ArrayDelegate { Property(it, this) }
    val staticProperties: List<Property> by ArrayDelegate { Property(it) }

    val methods: List<Method> by ArrayDelegate { Method(it, this) }
    val staticMethods: List<Method> by ArrayDelegate { Method(it) }

    override val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)

    private val extends: String? by NullableStringDelegate()
    private val implements: List<String> by StringArrayDelegate()

    override val classDeclaration = name + genericParameters()

    private val seeAlsoDoc: SeeAlso = SeeAlsoDoc(es6name ?: name)

    val documentation: String
        get() = getDocumentation(
            summary = summary,
            typeparameters = typeparameters,
            seeAlso = seeAlso + seeAlsoDoc
        )

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
    val events: List<Event> by ArrayDelegate { Event(it, this) }
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

    val constructors: List<Constructor> by ArrayDelegate { Constructor(it) }
}

internal class Interface(source: JSONObject) : ExtendedType(source)
internal class Enum(source: JSONObject) : Type(source)

internal sealed class SeeAlso {
    abstract fun toDoc(): String
}

internal class SeeAlsoType(override val source: JSONObject) : SeeAlso(), HasSource {
    private val type: String by StringDelegate()
    private val member: String? by NullableStringDelegate()

    private val doc: String by lazy {
        val cleanType = type.substringBefore("<")
        val cleanMember = member?.substringBefore("(")
            ?: return@lazy "[$cleanType]"

        "[$cleanType.$cleanMember]"
    }

    override fun toDoc(): String = doc

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeeAlsoType

        return doc == other.doc
    }

    override fun hashCode(): Int {
        return doc.hashCode()
    }
}

internal class SeeAlsoGuide(override val source: JSONObject) : SeeAlso(), HasSource {
    private val section: String by StringDelegate()
    private val name: String by StringDelegate()

    override fun toDoc(): String =
        link(
            text = name,
            href = "https://docs.yworks.com/yfileshtml/#/dguide/$section"
        )
}

internal class SeeAlsoDoc(private val id: String) : SeeAlso() {
    override fun toDoc(): String =
        link(
            text = "Online Documentation",
            href = "https://docs.yworks.com/yfileshtml/#/api/$id"
        )
}

private fun parseSeeAlso(source: JSONObject): SeeAlso =
    when {
        source.has("type") -> SeeAlsoType(source)
        source.has("section") -> SeeAlsoGuide(source)
        else -> throw IllegalArgumentException("Invalid SeeAlso source: $source")
    }

internal class Modifiers(flags: List<String>) {
    val static = flags.contains(STATIC)
    val final = flags.contains(FINAL)
    val readOnly = flags.contains(RO)
    val abstract = flags.contains(ABSTRACT)
    val protected = flags.contains(PROTECTED)

    private val canbenull = flags.contains(CANBENULL)
    val nullability = exp(canbenull, "?")
}

internal abstract class TypedDeclaration(source: JSONObject) : Declaration(source) {
    private val signature: String? by NullableStringDelegate()
    protected val type: String by TypeDelegate { parse(it, signature) }
}

internal class Constructor(source: JSONObject) : MethodBase(source) {
    val protected = modifiers.protected

    override val overridden: Boolean = false

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            parameters = parameters,
            seeAlso = seeAlso
        )

    override fun toCode(): String {
        val modificator: String = when {
            protected -> "protected"
            else -> ""
        }

        return "${modificator} constructor(${kotlinParametersString()})"
    }
}

internal class Constant(source: JSONObject) : TypedDeclaration(source) {
    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            seeAlso = seeAlso
        )

    override fun toCode(): String {
        return documentation +
                "val $name: $type"
    }

    fun toEnumValue(): String =
        documentation + name
}

internal class Property(
    source: JSONObject,
    private val parent: TypeDeclaration? = null
) : TypedDeclaration(source) {
    val static = modifiers.static
    val protected = modifiers.protected
    val getterSetter = !modifiers.readOnly

    val abstract = modifiers.abstract
    val final = modifiers.final
    val open = !static && !final

    private val overridden: Boolean
        get() = !static && ClassRegistry.instance.propertyOverriden(parent!!.classId, name)

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            seeAlso = seeAlso
        )

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
        return "$documentation$str"
    }

    override fun toExtensionCode(): String {
        require(!protected)
        requireNotNull(parent)

        val generics = getGenericString(parent.typeparameters)

        var str = "inline " + if (getterSetter) "var " else "val "
        str += "$generics ${parent.classDeclaration}.$name: $type${modifiers.nullability}\n" +
                "    get() = $AS_DYNAMIC.$name"
        if (getterSetter) {
            str += "\n    set(value) { $AS_DYNAMIC.$name = value }"
        }

        return "$documentation$str"
    }
}

private val OPERATOR_NAMES = setOf(
    "get",
    "contains"
)

internal class Method(
    source: JSONObject,
    private val parent: TypeDeclaration? = null
) : MethodBase(source) {
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
        get() = !static && ClassRegistry.instance.functionOverriden(parent!!.classId, name)

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            parameters = parameters,
            typeparameters = typeparameters,
            returns = returns,
            seeAlso = seeAlso
        )

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

        return documentation +
                "${kotlinModificator()} $operator fun $generics$name(${kotlinParametersString()})${getReturnSignature()}"
    }

    override fun toExtensionCode(): String {
        require(!protected)
        requireNotNull(parent)

        val extParameters = kotlinParametersString(extensionMode = true)
        val callParameters = parameters
            .byComma { it.name }

        val returnSignature = getReturnSignature()

        val generics = getGenericString(parent.typeparameters + typeparameters)
        val returnOperator = exp(returns != null, "return ")

        return documentation +
                "inline fun $generics ${parent.classDeclaration}.$name($extParameters)$returnSignature {\n" +
                "    $returnOperator $AS_DYNAMIC.$name($callParameters)\n" +
                "}"
    }
}

internal abstract class MethodBase(source: JSONObject) : Declaration(source) {
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
    val nullability = exp(canbenull, "?")
}

internal class Parameter(source: JSONObject) : JsonWrapper(source), IParameter {
    override val name: String by StringDelegate()
    private val signature: String? by NullableStringDelegate()
    val lambda: Boolean = signature != null
    val type: String by TypeDelegate { parse(it, signature) }
    override val summary: String? by SummaryDelegate()
    val modifiers: ParameterModifiers by ParameterModifiersDelegate()
}

internal class TypeParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    val summary: String? by SummaryDelegate()
}

internal class Returns(source: JSONObject) : JsonWrapper(source), IReturns {
    private val signature: String? by NullableStringDelegate()
    val type: String by TypeDelegate { parse(it, signature) }
    override val doc: String? by SummaryDelegate()
}

internal class Event(
    source: JSONObject,
    private val parent: TypeDeclaration
) : JsonWrapper(source) {
    val name: String by StringDelegate()
    private val summary: String? by SummaryDelegate()
    private val seeAlso: List<SeeAlso> by ArrayDelegate(::parseSeeAlso)
    private val add: EventListener by EventListenerDelegate(parent)
    private val remove: EventListener by EventListenerDelegate(parent)
    private val listeners = listOf(add, remove)

    val overriden by lazy {
        listeners.any { it.overriden }
    }

    val listenerNames: List<String>
        get() = listeners.map { it.name }

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            seeAlso = seeAlso
        )

    override fun toCode(): String {
        return documentation +
                listeners.lines { it.toCode() }
    }

    override fun toExtensionCode(): String {
        val generics = getGenericString(parent.typeparameters)
        val extensionName = "add${name}Handler"

        val listenerType = add.parameters.single().type
        val data = getHandlerData(listenerType)

        return documentation +
                """
                    inline fun $generics ${parent.classDeclaration}.$extensionName(
                        crossinline handler: ${data.handlerType}
                    ): () -> Unit {
                        val listener: $listenerType = ${data.listenerBody}
                        ${add.name}(listener)
                        return { ${remove.name}(listener) }
                    }
                """.trimIndent()
    }
}

private class EventListener(
    source: JSONObject,
    private val parent: HasClassId
) : JsonWrapper(source) {
    val name: String by StringDelegate()
    val modifiers: EventListenerModifiers by EventListenerModifiersDelegate()
    val parameters: List<Parameter> by ArrayDelegate(::Parameter)

    val overriden: Boolean
        get() = ClassRegistry.instance
            .listenerOverriden(parent.classId, name)

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

private class TypeDelegate(private val parse: (String) -> String) {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String {
        return parse(StringDelegate.value(thisRef, property))
    }
}

private class SummaryDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): String? {
        val value = NullableStringDelegate.value(thisRef, property)
            ?: return null

        return summary(value)
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

private class EventListenerDelegate(private val parent: HasClassId) {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): EventListener {
        return EventListener(thisRef.source.getJSONObject(property.name), parent)
    }
}

private class EventListenerModifiersDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): EventListenerModifiers {
        return EventListenerModifiers(StringArrayDelegate.value(thisRef, property))
    }
}

private fun getDocumentation(
    summary: String?,
    parameters: List<IParameter>? = null,
    typeparameters: List<TypeParameter>? = null,
    returns: IReturns? = null,
    seeAlso: List<SeeAlso>? = null
): String {
    val lines = mutableListOf<String>()
    if (summary != null) {
        lines.add(summary)
    }

    typeparameters?.apply {
        asSequence()
            .filter { it.summary != null }
            .mapTo(lines) { "@param [${it.name}] ${it.summary}" }
    }

    parameters?.apply {
        asSequence()
            .filter { it.summary != null }
            .mapTo(lines) { "@param ${it.name} ${it.summary}" }
    }

    returns?.doc?.let {
        lines.addAll("@return $it".split("\n"))
    }

    seeAlso?.apply {
        asSequence()
            .distinct()
            .mapTo(lines) {
                "@see ${it.toDoc()}"
            }
    }

    if (lines.isEmpty()) {
        return ""
    }

    return "/**\n" +
            lines.lines { " * $it" } +
            " */\n"
}

