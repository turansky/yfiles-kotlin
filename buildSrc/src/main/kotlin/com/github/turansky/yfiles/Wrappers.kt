package com.github.turansky.yfiles

import com.github.turansky.yfiles.json.*
import org.json.JSONObject

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
    val name: String by string()
    protected val modifiers: Modifiers by ModifiersDelegate()

    protected val summary: String? by summary()
    protected val remarks: String? by remarks()
    protected val seeAlso: List<SeeAlso> by list(::parseSeeAlso)

    override fun compareTo(other: Declaration): Int =
        name.compareTo(other.name)
}

internal class ApiRoot(source: JSONObject) : JsonWrapper(source) {
    private val namespaces: List<Namespace> by list(::Namespace)
    val rootTypes: List<Type>
        get() = namespaces
            .flatMap { it.types }

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

    val name: String by string()

    val namespaces: List<Namespace> by list(::Namespace)
    val types: List<Type> by declarationList { parseType(it) }
}

internal class FunctionSignature(fqn: ClassId, source: JSONObject) : JsonWrapper(source), HasClassId {
    override val classId = fixPackage(fqn)

    private val summary: String? by summary()
    private val seeAlso: List<SeeAlso> by list(::parseSeeAlso)

    private val parameters: List<SignatureParameter> by list(::SignatureParameter)
    private val typeparameters: List<TypeParameter> by list(::TypeParameter)
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
            .byCommaLine { it.toCode() }
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
    override val name: String by string()
    val type: String by TypeDelegate { parseType(it) }
    override val summary: String? by summary()

    // TODO: remove temp nullability fix
    override fun toCode(): String {
        val nullable = type == ANY
                && name != "source"
                && name != "sender"

        return "$name: $type" + exp(nullable, "?")
    }
}

internal interface IReturns {
    val doc: String?
}

internal class SignatureReturns(source: JSONObject) : JsonWrapper(source), IReturns {
    private val type: String by TypeDelegate { parseType(it) }
    override val doc: String? by summary()

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
    val docId: String
    val classDeclaration: String
    val generics: Generics
}

internal sealed class Type(source: JSONObject) : Declaration(source), TypeDeclaration {
    private val id: String by string()
    final override val classId: ClassId = fixPackage(id)

    val es6name: String? by optString()

    val constants: List<Constant> by declarationList { Constant(it, this) }

    val properties: List<Property> by declarationList { Property(it, this) }
    val staticProperties: List<Property> by declarationList { Property(it, this) }

    val methods: List<Method> by declarationList { Method(it, this) }
    val staticMethods: List<Method> by declarationList { Method(it, this) }

    private val typeparameters: List<TypeParameter> by list(::TypeParameter)
    final override val generics: Generics = Generics(typeparameters)

    private val extends: String? by optString()
    private val implements: List<String> by stringList()

    private val relatedDemos: List<Demo> by list(::Demo)

    final override val docId: String = es6name ?: name
    final override val classDeclaration = name + generics.asParameters()

    private val seeAlsoDoc: SeeAlso = SeeAlsoDoc(docId)

    protected open val additionalDocumentation: String? = null

    val documentation: String
        get() = getDocumentation(
            summary = summary,
            typeparameters = typeparameters,
            relatedDemos = relatedDemos,
            seeAlso = seeAlso + seeAlsoDoc,
            additionalDocumentation = additionalDocumentation
        )

    fun extendedType(): String? {
        val type = extends ?: return null
        return parseType(type)
    }

    fun implementedTypes(): List<String> {
        return implements.map { parseType(it) }
    }
}

internal sealed class ExtendedType(source: JSONObject) : Type(source) {
    val events: List<Event> by list { Event(it, this) }
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

    private val constructors: List<Constructor> by declarationList { Constructor(it, this) }
    val primaryConstructor: Constructor? = constructors.firstOrNull()
    val secondaryConstructors: List<Constructor> = constructors.drop(1)

    override val additionalDocumentation: String?
        get() = primaryConstructor?.getPrimaryDocumentation()
}

internal class Interface(source: JSONObject) : ExtendedType(source)
internal class Enum(source: JSONObject) : Type(source)

private class TypeReference(override val source: JSONObject) : HasSource {
    private val type: String by TypeDelegate { parseType(it) }
    private val member: String? by optString()

    fun toDoc(): String {
        val t = type.substringBefore("<")
        return member?.let {
            "[$t.$it]"
        } ?: "[$t]"
    }
}

private class DefaultValue(override val source: JSONObject) : HasSource {
    private val value: String? by optString()
    private val ref: TypeReference? by TypeReferenceDelegate()
    private val summary: String? by summary()

    private fun getDefault(): String {
        ref?.let {
            return it.toDoc()
        }

        val v = value!!
            .removeSuffix("d")

        return "`$v`"
    }

    fun toDoc(): String {
        var v = getDefault()

        v = "Default value - $v"
        summary?.let {
            v = "$v. $it"
        }
        return v
    }
}

private class ExceptionDescription(override val source: JSONObject) : HasSource {
    private val REDUNDANT_NULL_WARNING = Regex("if the specified .+ is `null`\\.?")

    private val name: String by string()
    private val summary: String? by summary()

    fun isEmpty(): Boolean =
        name == "NullReferenceError" || summary?.matches(REDUNDANT_NULL_WARNING) == true

    fun toDoc(): String =
        summary?.let {
            "$name $it"
        } ?: name
}

internal class Demo(override val source: JSONObject) : HasSource {
    private val path: String by string()
    private val text: String by string()
    private val summary: String by string()

    fun toDoc(): String {
        return link("$text 🚀", path)
    }
}

internal sealed class SeeAlso {
    abstract fun toDoc(): String
}

private class SeeAlsoType(override val source: JSONObject) : SeeAlso(), HasSource {
    private val type: String by string()
    private val member: String? by optString()

    private val doc: String by lazy {
        val cleanType = type.substringBefore("<")
        val cleanMember = member?.substringBefore("(")

        if (cleanMember == null || cleanType.endsWith(".$cleanMember")) {
            "[$cleanType]"
        } else {
            "[$cleanType.$cleanMember]"
        }
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

private class SeeAlsoGuide(override val source: JSONObject) : SeeAlso(), HasSource {
    private val section: String by string()
    private val name: String by string()

    override fun toDoc(): String =
        link(
            text = name,
            href = "$docBaseUrl/#/dguide/$section"
        )
}

private class SeeAlsoDoc(private val id: String) : SeeAlso() {
    constructor(typeId: String, memberId: String) : this("$typeId%23$memberId")

    override fun toDoc(): String =
        link(
            text = "Online Documentation",
            href = "$docBaseUrl/#/api/$id"
        )
}

private fun parseSeeAlso(source: JSONObject): SeeAlso =
    when {
        source.has("type") -> SeeAlsoType(source)
        source.has("section") -> SeeAlsoGuide(source)
        else -> throw IllegalArgumentException("Invalid SeeAlso source: $source")
    }

private fun seeAlsoDocs(
    parent: TypeDeclaration,
    memberId: String?
): List<SeeAlsoDoc> {
    val docId = memberId
        ?: return emptyList()

    return listOf(
        SeeAlsoDoc(parent.docId, docId)
    )
}

internal class Modifiers(flags: List<String>) {
    val static = STATIC in flags
    val final = FINAL in flags
    val readOnly = RO in flags
    val abstract = ABSTRACT in flags
    val protected = PROTECTED in flags

    private val canbenull = CANBENULL in flags
    val nullability = exp(canbenull, "?")

    val hidden = HIDDEN in flags
}

internal abstract class TypedDeclaration(
    source: JSONObject,
    protected val parent: TypeDeclaration
) : Declaration(source) {
    private val id: String? by optString()
    private val signature: String? by optString()
    protected val type: String by TypeDelegate {
        parse(it, signature).run {
            if (fixGeneric) asReadOnly() else this
        }
    }

    protected val seeAlsoDocs: List<SeeAlso>
        get() = seeAlsoDocs(parent, id)

    protected open val fixGeneric: Boolean
        get() = true
}

internal class Constructor(
    source: JSONObject,
    parent: Class
) : MethodBase(source, parent) {
    private val protected = modifiers.protected
    val public = !protected

    override val overridden: Boolean = false

    fun isDefault(): Boolean {
        return parameters.all { it.modifiers.optional }
    }

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            preconditions = preconditions,
            parameters = parameters,
            seeAlso = seeAlso + seeAlsoDocs
        )

    fun getPrimaryDocumentation(): String? {
        val lines = getDocumentationLines(
            summary = summary,
            preconditions = preconditions,
            parameters = parameters,
            seeAlso = seeAlso + seeAlsoDocs,
            primaryConstructor = true
        )

        return if (lines.isNotEmpty()) {
            lines.joinToString("\n")
        } else {
            null
        }
    }

    fun toPrimaryCode(): String {
        val declaration: String = when {
            protected -> "protected constructor"
            else -> ""
        }

        return "$declaration (${kotlinParametersString()})"
    }

    override fun toCode(): String {
        val modificator: String = when {
            protected -> "protected"
            else -> ""
        }

        return "$documentation$modificator constructor(${kotlinParametersString()})"
    }
}

internal class Constant(
    source: JSONObject,
    parent: TypeDeclaration
) : TypedDeclaration(source, parent) {
    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            seeAlso = seeAlso + seeAlsoDocs
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
    parent: TypeDeclaration
) : TypedDeclaration(source, parent) {
    val static = modifiers.static
    private val protected = modifiers.protected
    val public = !protected
    val getterSetter = !modifiers.readOnly

    val abstract = modifiers.abstract
    private val final = modifiers.final
    private val open = !static && !final

    private val preconditions: List<String> by stringList(::summary)

    private val defaultValue: DefaultValue? by DefaultValueDelegate()

    private val throws: List<ExceptionDescription> by list(::ExceptionDescription)

    private val overridden: Boolean
        get() = !static && ClassRegistry.instance.propertyOverridden(parent.classId, name)

    override val fixGeneric: Boolean
        get() = name != "children"

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            remarks = remarks,
            preconditions = preconditions,
            defaultValue = defaultValue,
            exceptions = throws.filterNot { it.isEmpty() },
            seeAlso = seeAlso + seeAlsoDocs
        )

    override fun toCode(): String {
        var str = ""

        if (overridden) {
            str += exp(final, "final ") + "override "
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

        val generics = parent.generics.declaration

        var str = "inline " + if (getterSetter) "var " else "val "
        str += "$generics ${parent.classDeclaration}.$name: $type${modifiers.nullability}\n" +
                "    get() = $AS_DYNAMIC.$name"
        if (getterSetter) {
            str += "\n    set(value) { $AS_DYNAMIC.$name = value }"
        }

        return "$documentation$str"
    }
}

private val OPERATOR_MAP = mapOf(
    "get" to 1,
    "set" to 2,
    "contains" to 1,
    "compareTo" to 1
)

internal class Method(
    source: JSONObject,
    private val parent: Type
) : MethodBase(source, parent) {
    val abstract = modifiers.abstract
    private val static = modifiers.static
    private val protected = modifiers.protected

    private val final = modifiers.final
    private val open = !static && !final

    private val hidden = modifiers.hidden

    private val isExtension by boolean()

    private val postconditions: List<String> by stringList(::summary)

    private val typeparameters: List<TypeParameter> by list(::TypeParameter)
    val generics: Generics = Generics(typeparameters)

    val returns: Returns? by ReturnsDelegate()

    private val throws: List<ExceptionDescription> by list(::ExceptionDescription)

    override val overridden: Boolean
        get() = !static && ClassRegistry.instance.functionOverridden(parent.classId, name)

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            remarks = remarks,
            preconditions = preconditions,
            postconditions = postconditions,
            parameters = parameters,
            typeparameters = typeparameters,
            returns = returns,
            exceptions = throws.filterNot { it.isEmpty() },
            seeAlso = seeAlso + seeAlsoDocs
        )

    private fun kotlinModificator(): String {
        if (isExtension) {
            require(!protected)
            require(!abstract)
            return ""
        }

        if (overridden) {
            return exp(final, "final ") + exp(abstract, "abstract ") + "override "
        }

        return when {
            abstract -> "abstract "
            final -> "final "
            open -> "open "
            else -> ""
        } + exp(protected, "protected ")
    }

    private fun nullablePromiseResult(generic: String): Boolean =
        when (generic) {
            "String" -> name == "editLabelCore" || name == "edit"
            "yfiles.graph.IEdge",
            "yfiles.graph.ILabel",
            "yfiles.collections.IEnumerable<yfiles.graph.IModelItem>" -> true
            else -> false
        }

    // https://youtrack.jetbrains.com/issue/KT-31249
    private fun getReturnSignature(): String {
        var type = returns?.type
            ?: return ""

        if (type.startsWith("$PROMISE<")) {
            val generic = between(type, "<", ">")
            val newGeneric = if (generic == ANY) {
                "Nothing?"
            } else if (generic == ELEMENT) {
                SVG_SVG_ELEMENT
            } else {
                generic + exp(nullablePromiseResult(generic), "?")
            }
            type = "$PROMISE<$newGeneric>"
        }

        return ":" + type + modifiers.nullability
    }

    private fun toCode(
        methodName: String,
        operatorMode: Boolean
    ): String {
        val operator = exp(operatorMode, " operator ")

        val code = "${kotlinModificator()} $operator fun ${generics.declaration}$methodName(${kotlinParametersString()})${getReturnSignature()}"
        if (!hidden) {
            return code
        }

        return """
            |$HIDDEN_METHOD_ANNOTATION
            |$code
        """.trimMargin()
    }

    override fun toCode(): String {
        val operatorMode = OPERATOR_MAP[name] == parameters.size
                && parameters.first().name != "x" // to exclude RectangleHandle.set

        return documentation +
                toCode(name, operatorMode) +
                toOperatorCode()
    }

    private fun toOperatorCode(): String {
        val operatorName = when {
            name == "add" && parameters.size == 1 && returns != null -> "plus"
            name == "subtrack" && parameters.size == 1 && returns != null -> "minus"
            name == "multiply" && parameters.size == 1 && returns != null -> "times"

            name == "add" && parameters.size == 1 && returns == null -> "plusAssign"
            name == "remove" && parameters.size == 1 && returns == null -> "minusAssign"
            else -> return ""
        }

        val jsName = exp(
            !overridden,
            """@JsName("$name")"""
        )

        return """
                |
                |$jsName
                |${toCode(operatorName, true)}
               """.trimMargin()
    }

    override fun toExtensionCode(): String {
        require(!protected)
        requireNotNull(parent)

        val extParameters = kotlinParametersString(extensionMode = true)
        val callParameters = parameters
            .byComma { it.name }

        val returnSignature = getReturnSignature()

        val genericDeclaration = (parent.generics + generics).declaration
        val returnOperator = exp(returns != null, "return ")

        val methodCall = if (parameters.none { it.modifiers.vararg }) {
            "$name($callParameters)"
        } else {
            require(parameters.size == 1)
            "$name.apply(this, $callParameters)"
        }

        return documentation +
                "inline fun $genericDeclaration ${parent.classDeclaration}.$name($extParameters)$returnSignature {\n" +
                "    $returnOperator $AS_DYNAMIC.$methodCall\n" +
                "}"
    }
}

private val EXCLUDED_READ_ONLY = setOf(
    "copyTo",
    "toArray"
)

internal abstract class MethodBase(
    source: JSONObject,
    private val parent: Type
) : Declaration(source) {
    private val id: String? by optString()
    protected val parameters: List<Parameter> by list { Parameter(it, name !in EXCLUDED_READ_ONLY) }
    val options: Boolean by boolean()

    protected val preconditions: List<String> by stringList(::summary)

    protected abstract val overridden: Boolean

    protected val seeAlsoDocs: List<SeeAlso>
        get() = seeAlsoDocs(parent, id)

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

    override fun compareTo(other: Declaration): Int {
        val result = super.compareTo(other)
        if (result != 0) {
            return result
        }

        if (other !is MethodBase) {
            return result
        }

        return compareValuesBy(this, other) {
            it.parameters.size
        }
    }
}

internal class ParameterModifiers(flags: List<String>) {
    val artificial = ARTIFICIAL in flags
    val vararg = VARARGS in flags
    val optional = OPTIONAL in flags
    val conversion = CONVERSION in flags

    private val canbenull = CANBENULL in flags
    val nullability = exp(canbenull, "?")
}

internal class Parameter(
    source: JSONObject,
    private val readOnly: Boolean = true
) : JsonWrapper(source), IParameter {
    override val name: String by string()
    private val signature: String? by optString()
    val lambda: Boolean = signature != null
    val type: String by TypeDelegate { parse(it, signature).inMode(readOnly) }
    override val summary: String? by summary()
    val modifiers: ParameterModifiers by ParameterModifiersDelegate()
}

internal class TypeParameter(source: JSONObject) : JsonWrapper(source), IParameter {
    override val name: String by string()
    override val summary: String? by summary()
    private val bounds: List<String> by stringList()

    init {
        require(bounds.size <= 1)
    }

    override fun toCode(): String =
        if (bounds.isNotEmpty()) {
            val bound = parseType(bounds.first())
            "$name : $bound"
        } else {
            name
        }
}

internal class Generics(private val parameters: List<TypeParameter>) {
    val declaration: String
        get() = if (parameters.isNotEmpty()) {
            "<${parameters.byComma { it.toCode() }}> "
        } else {
            ""
        }

    fun asParameters(): String =
        if (parameters.isNotEmpty()) {
            "<${parameters.byComma { it.name }}> "
        } else {
            ""
        }

    val placeholder: String
        get() = if (parameters.isNotEmpty()) {
            "<" + (1..parameters.size).map { "*" }.joinToString(",") + ">"
        } else {
            ""
        }

    fun isEmpty(): Boolean =
        parameters.isEmpty()

    fun isNotEmpty(): Boolean =
        !isEmpty()

    operator fun plus(other: Generics): Generics =
        Generics(parameters + other.parameters)
}

internal class Returns(source: JSONObject) : JsonWrapper(source), IReturns {
    private val signature: String? by optString()
    val type: String by TypeDelegate { parse(it, signature).asReadOnly() }
    override val doc: String? by summary()
}

internal class Event(
    source: JSONObject,
    private val parent: TypeDeclaration
) : JsonWrapper(source) {
    private val id: String by string()
    val name: String by string()
    private val summary: String? by summary()
    private val seeAlso: List<SeeAlso> by list(::parseSeeAlso)
    private val add: EventListener by EventListenerDelegate(parent)
    private val remove: EventListener by EventListenerDelegate(parent)
    private val listeners = listOf(add, remove)

    val overriden by lazy {
        listeners.any { it.overriden }
    }

    private val seeAlsoDocs: List<SeeAlso>
        get() = seeAlsoDocs(parent, id)

    val listenerNames: List<String>
        get() = listeners.map { it.name }

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            seeAlso = seeAlso + seeAlsoDocs
        )

    override fun toCode(): String {
        return documentation +
                listeners.lines { it.toCode() }
    }

    override fun toExtensionCode(): String {
        val generics = parent.generics.declaration
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
    val name: String by string()
    val modifiers: EventListenerModifiers by EventListenerModifiersDelegate()
    val parameters: List<Parameter> by list { Parameter(it) }

    val overriden: Boolean
        get() = ClassRegistry.instance
            .listenerOverridden(parent.classId, name)

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
    val public = PUBLIC in flags
    val abstract = ABSTRACT in flags
}

private class TypeDelegate(private val parse: (String) -> String) : JsonDelegate<String>() {
    override fun read(
        source: JSONObject,
        key: String
    ): String {
        return parse(StringDelegate.value(source, key))
    }
}

private fun summary(): JsonDelegate<String?> = SummaryDelegate()

private class SummaryDelegate : JsonDelegate<String?>() {
    override fun read(
        source: JSONObject,
        key: String
    ): String? {
        val value = NullableStringDelegate.value(source, key)
            ?: return null

        return summary(value)
    }
}

private fun remarks(): JsonDelegate<String?> = RemarksDelegate()

private class RemarksDelegate : JsonDelegate<String?>() {
    override fun read(
        source: JSONObject,
        key: String
    ): String? {
        val value = NullableStringDelegate.value(source, key)
            ?.takeIf { it.startsWith("The default ") }
            ?: return null

        return summary(value)
    }
}

private class ModifiersDelegate : JsonDelegate<Modifiers>() {
    override fun read(
        source: JSONObject,
        key: String
    ): Modifiers {
        return Modifiers(StringArrayDelegate.value(source, key))
    }
}

private class ParameterModifiersDelegate : JsonDelegate<ParameterModifiers>() {
    override fun read(
        source: JSONObject,
        key: String
    ): ParameterModifiers {
        return ParameterModifiers(StringArrayDelegate.value(source, key))
    }
}

private class SignatureReturnsDelegate : JsonDelegate<SignatureReturns?>() {
    override fun read(
        source: JSONObject,
        key: String
    ): SignatureReturns? {
        return if (source.has(key)) {
            SignatureReturns(source.getJSONObject(key))
        } else {
            null
        }
    }
}

private class ReturnsDelegate : JsonDelegate<Returns?>() {
    override fun read(
        source: JSONObject,
        key: String
    ): Returns? {
        return if (source.has(key)) {
            Returns(source.getJSONObject(key))
        } else {
            null
        }
    }
}

private class TypeReferenceDelegate : JsonDelegate<TypeReference?>() {
    override fun read(
        source: JSONObject,
        key: String
    ): TypeReference? {
        return if (source.has(key)) {
            TypeReference(source.getJSONObject(key))
        } else {
            null
        }
    }
}

private class DefaultValueDelegate : JsonDelegate<DefaultValue?>() {
    private val KEY = "y.default"

    override fun read(
        source: JSONObject,
        key: String
    ): DefaultValue? {
        if (!source.has(KEY)) {
            return null
        }

        val s = source.getJSONObject(KEY)
        return if (s.has("value") || s.has("ref")) {
            DefaultValue(s)
        } else {
            null
        }
    }
}

private class EventListenerDelegate(private val parent: HasClassId) : JsonDelegate<EventListener>() {
    override fun read(
        source: JSONObject,
        key: String
    ): EventListener {
        return EventListener(source.getJSONObject(key), parent)
    }
}

private class EventListenerModifiersDelegate : JsonDelegate<EventListenerModifiers>() {
    override fun read(
        source: JSONObject,
        key: String
    ): EventListenerModifiers {
        return EventListenerModifiers(StringArrayDelegate.value(source, key))
    }
}

private fun <T : Declaration> declarationList(
    transform: (JSONObject) -> T
): JsonDelegate<List<T>> = DeclarationArrayDelegate(transform)

private class DeclarationArrayDelegate<T : Declaration>(
    transform: (JSONObject) -> T
) : ArrayDelegate<T>(transform) {

    override fun read(
        source: JSONObject,
        key: String
    ): List<T> {
        return super.read(source, key)
            .sorted()
    }
}

private fun getDocumentation(
    summary: String?,
    remarks: String? = null,
    preconditions: List<String>? = null,
    postconditions: List<String>? = null,
    parameters: List<IParameter>? = null,
    typeparameters: List<TypeParameter>? = null,
    returns: IReturns? = null,
    defaultValue: DefaultValue? = null,
    exceptions: List<ExceptionDescription>? = null,
    relatedDemos: List<Demo>? = null,
    seeAlso: List<SeeAlso>? = null,
    additionalDocumentation: String? = null
): String {
    var lines = getDocumentationLines(
        summary = summary,
        remarks = remarks,
        preconditions = preconditions,
        postconditions = postconditions,
        parameters = parameters,
        typeparameters = typeparameters,
        returns = returns,
        defaultValue = defaultValue,
        exceptions = exceptions,
        relatedDemos = relatedDemos,
        seeAlso = seeAlso
    )

    additionalDocumentation?.apply {
        if (lines.isNotEmpty()) {
            lines = lines + ""
        }

        lines = lines + split("\n")
    }

    if (lines.isEmpty()) {
        return ""
    }

    return "/**\n" +
            lines.lines { " * $it" } +
            " */\n"
}

private fun getDocumentationLines(
    summary: String?,
    remarks: String? = null,
    preconditions: List<String>? = null,
    postconditions: List<String>? = null,
    parameters: List<IParameter>? = null,
    typeparameters: List<TypeParameter>? = null,
    returns: IReturns? = null,
    defaultValue: DefaultValue? = null,
    exceptions: List<ExceptionDescription>? = null,
    relatedDemos: List<Demo>? = null,
    seeAlso: List<SeeAlso>? = null,
    primaryConstructor: Boolean = false
): List<String> {
    val lines = mutableListOf<String>()
    if (summary != null) {
        if (primaryConstructor) {
            lines.add(constructor(summary))
        } else {
            lines.addAll(summary.split(LINE_DELIMITER))
        }
    }

    remarks?.let {
        if (lines.isNotEmpty()) {
            lines.add("")
        }

        lines.add(it)
    }

    lines.addAll(preconditions.toNamedList("Preconditions"))
    lines.addAll(postconditions.toNamedList("Postconditions"))

    typeparameters?.flatMapTo(lines) {
        it.toDoc()
    }

    parameters?.flatMapTo(lines) {
        it.toDoc()
    }

    returns?.doc?.let {
        lines.addAll(ret(it))
    }

    defaultValue?.let {
        if (lines.isNotEmpty()) {
            lines.add("")
        }

        lines.add(it.toDoc())
    }

    exceptions?.mapTo(lines) {
        throws(it.toDoc())
    }

    relatedDemos?.mapTo(lines) {
        see(it.toDoc())
    }

    seeAlso?.apply {
        asSequence()
            .distinct()
            .mapTo(lines) {
                see(it.toDoc())
            }
    }

    if (primaryConstructor && summary == null && lines.isNotEmpty()) {
        lines.add(0, constructor())
    }

    return lines.toList()
}

private fun IParameter.toDoc(): List<String> {
    val summary = summary
        ?: return emptyList()

    return param(name, summary)
}

private fun List<String>?.toNamedList(title: String): List<String> {
    if (this == null || isEmpty()) {
        return emptyList()
    }

    return listOf("### $title") + map(::listItem)
}

