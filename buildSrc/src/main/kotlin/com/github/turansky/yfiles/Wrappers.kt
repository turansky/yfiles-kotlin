package com.github.turansky.yfiles

import com.github.turansky.yfiles.PropertyMode.WRITE_ONLY
import com.github.turansky.yfiles.correction.GROUP
import com.github.turansky.yfiles.correction.get
import com.github.turansky.yfiles.json.*
import org.json.JSONObject

internal sealed class JsonWrapper(override val source: JSONObject) : HasSource {
    open fun toCode(): String =
        throw IllegalStateException("toCode() method must be overridden")

    open fun toExtensionCode(): String =
        throw IllegalStateException("toExtensionCode() method must be overridden")

    final override fun toString(): String =
        throw IllegalStateException("Use method toCode() instead")
}

internal sealed class Declaration(source: JSONObject) : JsonWrapper(source), Comparable<Declaration> {
    val name: String by string()

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

    val functionSignatures: List<FunctionSignature> by list(::FunctionSignature)
}

private class Namespace(source: JSONObject) : JsonWrapper(source) {
    companion object {
        fun parseType(source: JSONObject): Type =
            when (val group = source[GROUP]) {
                "class" -> Class(source)
                "interface" -> Interface(source)
                "enum" -> Enum(source)
                else -> throw IllegalArgumentException("Undefined type group '$group'")
            }
    }

    val name: String by string()

    val namespaces: List<Namespace> by list(::Namespace)
    val types: List<Type> by list(::parseType)
}

internal class FunctionSignature(source: JSONObject) : JsonWrapper(source), HasClassId {
    private val id: String by string()
    override val classId = id

    private val summary: String? by summary()
    private val seeAlso: List<SeeAlso> by list(::parseSeeAlso)

    private val parameters: List<SignatureParameter> by list(::SignatureParameter)
    private val typeparameters: List<TypeParameter> by list(::TypeParameter)
    private val returns: SignatureReturns? by optNamed(::SignatureReturns)

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
    private val type: String by type { parseType(it) }
    private val modifiers: ParameterModifiers by parameterModifiers()
    override val summary: String? by summary()

    override fun toCode(): String {
        return "$name: $type${modifiers.nullability}"
    }
}

internal interface IReturns {
    val doc: String?
}

internal class SignatureReturns(source: JSONObject) : JsonWrapper(source), IReturns {
    private val type: String by type { parseType(it) }
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
    final override val classId: ClassId = id

    val es6name: String? by optString()

    abstract val constants: List<Constant>

    val properties: List<Property> by declarationList(::Property)
    val staticProperties: List<Property> by declarationList(::Property)

    val methods: List<Method> by declarationList(::Method)
    val staticMethods: List<Method> by declarationList(::Method)
    val extensionMethods: List<Method> by lazy {
        methods.mapNotNull { it.toOperatorExtension() } + staticMethods.mapNotNull { it.toStaticOperatorExtension() }
    }

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
    override val constants: List<Constant> by declarationList(::TypeConstant)

    val events: List<Event> by list { Event(it, this) }
}

internal class Class(source: JSONObject) : ExtendedType(source) {
    private val modifiers: ClassModifiers by wrapStringList(::ClassModifiers)
    val final: Boolean = modifiers.mode == ClassMode.FINAL
    val abstract: Boolean = modifiers.mode == ClassMode.ABSTRACT

    val kotlinModifier: String = when (modifiers.mode) {
        ClassMode.FINAL -> ""
        ClassMode.OPEN -> "open"
        ClassMode.SEALED -> "sealed"
        ClassMode.ABSTRACT -> "abstract"
    }

    private val constructors: List<Constructor> by declarationList(::Constructor)
    val primaryConstructor: Constructor? = constructors.firstOrNull()
    val secondaryConstructors: List<Constructor> = constructors.drop(1)

    override val additionalDocumentation: String?
        get() = primaryConstructor?.getPrimaryDocumentation()
}

internal class Interface(source: JSONObject) : ExtendedType(source)

internal class Enum(source: JSONObject) : Type(source) {
    private val modifiers: EnumModifiers by wrapStringList(::EnumModifiers)
    val flags = modifiers.flags
    override val constants: List<Constant> by declarationList(::EnumConstant)
}

private class TypeReference(override val source: JSONObject) : HasSource {
    private val type: String by type { parseType(it) }
    private val member: String? by optString()

    fun toDoc(): String {
        val t = type.substringBefore("<")
        return member?.let {
            "[$t.$it]"
        } ?: "[$t]"
    }
}

private class Value(private val value: Int) {
    fun toDoc(): String {
        return "Value - `$value`"
    }
}

private class DefaultValue(override val source: JSONObject) : HasSource {
    private val value: String? by optString()
    private val ref: TypeReference? by optNamed(::TypeReference)
    private val summary: String? by summary()

    fun isNotEmpty(): Boolean =
        value != null || ref != null

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

private class DpData(override val source: JSONObject) : HasSource {
    private val domain: DpDataItem by dpDataItem()
    private val values: DpDataItem by dpDataItem()

    fun toDoc(): List<String> =
        listOf(
            "Domain - ${domain.toDoc()}",
            "Values - ${values.toDoc()}"
        )
}

private class DpDataItem(override val source: JSONObject) : HasSource {
    private val type: String by string()
    private val summary: String? by summary()

    fun toDoc(): String {
        val summary = summary
            ?: return "[$type]"

        return "[$type] $summary"
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
            href = "$DOC_BASE_URL/#/dguide/$section"
        )
}

private class SeeAlsoDoc(private val id: String) : SeeAlso() {
    constructor(typeId: String, memberId: String) : this("$typeId#$memberId")

    override fun toDoc(): String =
        link(
            text = "Online Documentation",
            href = "$DOC_BASE_URL/#/api/$id"
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

internal sealed class TypedDeclaration(
    source: JSONObject,
    protected val parent: TypeDeclaration
) : Declaration(source) {
    private val id: String? by optString()
    private val signature: String? by optString()
    protected val type: String by type {
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
    private val modifiers: ConstructorModifiers by wrapStringList(::ConstructorModifiers)
    val public = modifiers.visibility == ConstructorVisibility.PUBLIC

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
        val declaration: String = when (modifiers.visibility) {
            ConstructorVisibility.PUBLIC -> ""
            ConstructorVisibility.PROTECTED -> "\nprotected constructor"
            ConstructorVisibility.INTERNAL -> "\ninternal constructor"
        }

        return "$declaration (${kotlinParametersString()})"
    }

    override fun toCode(): String {
        val modificator: String = when (modifiers.visibility) {
            ConstructorVisibility.PROTECTED -> "protected"
            else -> ""
        }

        return "$documentation$modificator constructor(${kotlinParametersString()})"
    }
}

internal sealed class Constant(
    source: JSONObject,
    parent: TypeDeclaration
) : TypedDeclaration(source, parent) {
    abstract fun toEnumValue(): String
}

private class TypeConstant(
    source: JSONObject,
    parent: TypeDeclaration
) : Constant(source, parent) {
    private val dpdata: DpData? by optNamed(::DpData)

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            remarks = remarks,
            seeAlso = seeAlso + seeAlsoDocs
        )

    override fun toCode(): String =
        documentation +
                "val $name: $type"

    override fun toEnumValue(): String =
        documentation + name
}

private class EnumConstant(
    source: JSONObject,
    parent: TypeDeclaration
) : Constant(source, parent) {
    private val value: Int by int()

    private val documentation: String
        get() = getDocumentation(
            summary = summary,
            value = Value(value),
            seeAlso = seeAlso + seeAlsoDocs
        )

    override fun toCode(): String =
        toEnumValue()

    override fun toEnumValue(): String =
        documentation + name

    override fun compareTo(other: Declaration): Int =
        when (other) {
            is EnumConstant -> value - other.value
            else -> super.compareTo(other)
        }
}

internal class Property(
    source: JSONObject,
    parent: TypeDeclaration
) : TypedDeclaration(source, parent) {
    private val modifiers: PropertyModifiers by wrapStringList(::PropertyModifiers)
    val static = modifiers.static
    private val protected = modifiers.protected
    val public = !protected
    val mode = modifiers.mode

    val abstract = modifiers.abstract
    private val final = modifiers.final
    private val open = !static && !final

    private val preconditions: List<String> by stringList(::summary)

    private val defaultValue: DefaultValue? by defaultValue()

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

        str += if (mode.writable) "var " else "val "

        str += "$name: $type${modifiers.nullability}"
        if (mode == WRITE_ONLY) {
            str += """
                |
                |   @Deprecated(message = "Write-only property", level = DeprecationLevel.HIDDEN)
                |   get() = definedExternally
            """.trimMargin()
        }

        return "$documentation$str"
    }

    override fun toExtensionCode(): String {
        require(!protected)
        require(mode != WRITE_ONLY)

        val generics = parent.generics.declaration

        var str = "inline " + if (mode.writable) "var " else "val "
        str += "$generics ${parent.classDeclaration}.$name: $type${modifiers.nullability}\n" +
                "    get() = $AS_DYNAMIC.$name"
        if (mode.writable) {
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

private val OPERATOR_NAME_MAP = mapOf(
    "add" to "plus",
    "getEnlarged" to "plus",

    "subtract" to "minus",
    "getReduced" to "minus",

    "multiply" to "times",

    "includes" to "contains"
)

private val ASSIGN_OPERATOR_NAME_MAP = mapOf(
    "add" to "plusAssign",
    "remove" to "minusAssign"
)

private val INFIX_METHODS = setOf(
    "intersects",
    "distance",
    "distanceSq",
    "distanceTo",
    "indexOf",
    "scalarProduct",
    "supports",
    "lookup",
    "canDecorate",
    "combineWith",
    "isGreaterThan",
    "isLessThan",
    "coveredBy",
    "crosses",
    "hasSameRange",
    "manhattanDistanceTo",
    "equalValues",
    "above",
    "below"
)

internal class Method(
    source: JSONObject,
    private val parent: Type
) : MethodBase(source, parent) {
    // TODO: Move to constructor in Kotlin 1.4
    private var operatorName: String? = null

    private val modifiers: MethodModifiers by wrapStringList(::MethodModifiers)
    val abstract = modifiers.abstract
    private val static = modifiers.static
    private val protected = modifiers.protected

    private val final = modifiers.final
    private val open = !static && !final

    private val hidden = modifiers.hidden

    private val isExtension by boolean()

    private val postconditions: List<String> by stringList(::summary)

    private val typeparameters: List<TypeParameter> by list(::TypeParameter)
    private val generics: Generics = Generics(typeparameters)

    val returns: Returns? by optNamed(::Returns)

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

        val infix = when {
            parameters.size != 1 -> ""
            returns == null -> ""
            name !in INFIX_METHODS -> ""
            else -> "infix "
        }

        return when {
            abstract -> "abstract "
            final -> "final "
            open -> "open "
            else -> ""
        } + exp(protected, "protected ") + infix
    }

    private fun nullablePromiseResult(generic: String): Boolean =
        when (generic) {
            "String" -> name == "editLabelCore" || name == "edit"
            IEDGE,
            ILABEL,
            "$IENUMERABLE<$IMODEL_ITEM>" -> true
            else -> false
        }

    // https://youtrack.jetbrains.com/issue/KT-31249
    private fun getReturnSignature(): String {
        var type = returns?.type ?: return ""

        if (type.startsWith("$PROMISE<")) {
            val newGeneric = when (val generic = type.between("<", ">")) {
                ANY -> "Nothing?"
                ELEMENT -> SVG_SVG_ELEMENT
                else -> generic + exp(nullablePromiseResult(generic), "?")
            }
            type = "$PROMISE<$newGeneric>"
        }

        return ":" + type + modifiers.nullability
    }

    private fun isOperatorMode(): Boolean =
        OPERATOR_MAP[name] == parameters.size
                && parameters.first().name != "x" // to exclude RectangleHandle.set

    override fun toCode(): String {
        val operator = exp(isOperatorMode(), "operator")

        var code = "${kotlinModificator()} $operator fun ${generics.declaration}$name(${kotlinParametersString()})${getReturnSignature()}"
        if (hidden) {
            code = HIDDEN_METHOD_ANNOTATION + "\n" + code
        }

        return documentation + code
    }

    fun toStaticOperatorExtension(): Method? {
        val operatorName = OPERATOR_NAME_MAP[name] ?: return null
        if (parameters.size != 2) return null
        val returns = returns ?: return null

        setOf(
            parent.classId,
            parameters[0].type,
            parameters[1].type,
            returns.type
        ).singleOrNull() ?: return null

        return Method(source, parent)
            .also { it.operatorName = operatorName }
    }

    fun toOperatorExtension(): Method? {
        when {
            parameters.size != 1 -> return null
            protected -> return null
            overridden -> return null
        }

        val removeMethod = name == "remove"
        if (removeMethod && parameters[0].type == INT) return null

        val assignMode = returns.let { it == null || (removeMethod && it.type == BOOLEAN) }
        val operatorName = if (assignMode) {
            ASSIGN_OPERATOR_NAME_MAP[name]
        } else {
            OPERATOR_NAME_MAP[name]
        } ?: return null

        val newSource = if (assignMode && returns != null) {
            JSONObject(source, (source.keySet() - "returns").toTypedArray())
        } else {
            source
        }

        return Method(newSource, parent)
            .also { it.operatorName = operatorName }
    }

    override fun toExtensionCode(): String {
        if (static) {
            return toStaticExtensionCode()
        }

        require(!protected)

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

        val extensionName = operatorName ?: name
        val operator = exp(operatorName != null || isOperatorMode(), "operator")
        return documentation +
                "inline $operator fun $genericDeclaration ${parent.classDeclaration}.$extensionName($extParameters)$returnSignature {\n" +
                "    $returnOperator $AS_DYNAMIC.$methodCall\n" +
                "}"
    }

    private fun toStaticExtensionCode(): String {
        val type = parent.name
        val parameter = parameters[1]

        return """
            inline operator fun $type.$operatorName(${parameter.name}: ${parameter.type}): $type {
                return $type.$name(this, ${parameter.name})
            }
        """.trimIndent()
    }
}

private val EXCLUDED_READ_ONLY = setOf(
    "copyTo",
    "toArray"
)

internal sealed class MethodBase(
    source: JSONObject,
    private val parent: Type
) : Declaration(source) {
    private val id: String? by optString()
    val parameters: List<Parameter> by list { Parameter(it, name !in EXCLUDED_READ_ONLY) }
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

internal class Parameter(
    source: JSONObject,
    private val readOnly: Boolean = true
) : JsonWrapper(source), IParameter {
    override val name: String by string()
    private val signature: String? by optString()
    val lambda: Boolean = signature != null
    val type: String by type { parse(it, signature).inMode(readOnly) }
    override val summary: String? by summary()
    val modifiers: ParameterModifiers by parameterModifiers()
}

internal interface ITypeParameter {
    val name: String

    fun toCode(): String
}

internal class TypeParameter(source: JSONObject) : JsonWrapper(source), IParameter, ITypeParameter {
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

internal class CustomTypeParameter(
    override val name: String,
    private val bound: String
) : ITypeParameter {
    override fun toCode(): String =
        "$name : $bound"
}

internal class Generics(private val parameters: List<ITypeParameter>) {
    val declaration: String
        get() = if (parameters.isNotEmpty()) {
            "<${parameters.byComma { it.toCode() }}> "
        } else {
            ""
        }

    fun asParameters(): String =
        asParameters { it }

    fun asAliasParameters(): String =
        asParameters { it.removePrefix("in ").removePrefix("out ") }

    private fun asParameters(transform: (String) -> String): String =
        if (parameters.isNotEmpty()) {
            "<${parameters.byComma { transform(it.name) }}> "
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
    val type: String by type { parse(it, signature).asReadOnly() }
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
    private val add: EventListener by eventListener(parent)
    private val remove: EventListener by eventListener(parent)
    private val listeners = listOf(add, remove)

    val overriden by lazy {
        listeners.any { it.overridden }
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
        // TODO: fix in common way
        val generics = parent.generics.declaration.replace("<in T", "<T")
        val classDeclaration = parent.classDeclaration.replace("<in T>", "<T>")
        val extensionName = "add${name}Handler"

        val listenerType = add.parameters.single().type
        val data = getHandlerData(listenerType)

        return documentation +
                """
                    inline fun $generics $classDeclaration.$extensionName(
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
    val modifiers: EventListenerModifiers by wrapStringList(::EventListenerModifiers)
    val parameters: List<Parameter> by list { Parameter(it) }

    val overridden: Boolean
        get() = ClassRegistry.instance
            .listenerOverridden(parent.classId, name)

    private fun kotlinModificator(): String {
        return when {
            overridden -> "override "
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

private fun type(
    parse: (String) -> String
): Prop<String> =
    string(parse)

private fun summary(): Prop<String?> = SummaryDelegate()

private class SummaryDelegate : PropDelegate<String?>() {
    override fun read(
        source: JSONObject,
        key: String
    ): String? {
        val value = optString(source, key)
            ?: return null

        return summary(value)
    }
}

private fun remarks(): Prop<String?> = RemarksDelegate()

private class RemarksDelegate : PropDelegate<String?>() {
    private fun String.isSummaryLike(): Boolean =
        startsWith("The default ") or startsWith("By default ") or endsWith("then <code>null</code> is returned.")

    private fun JSONObject.isRequiredRemarks(): Boolean =
        optString("id")?.startsWith("ICommand-field-") ?: false

    override fun read(
        source: JSONObject,
        key: String
    ): String? {
        val value = optString(source, key)
            ?.takeIf { it.isSummaryLike() or source.isRequiredRemarks() }
            ?: return null

        return summary(value)
    }
}

private fun parameterModifiers(): Prop<ParameterModifiers> =
    wrapStringList(::ParameterModifiers)

private fun dpDataItem(): Prop<DpDataItem> =
    named(::DpDataItem)

private fun defaultValue(): Prop<DefaultValue?> =
    optNamed("y.default") {
        DefaultValue(it)
            .takeIf { it.isNotEmpty() }
    }

private fun eventListener(parent: HasClassId): Prop<EventListener> =
    named { EventListener(it, parent) }

private fun <P : Declaration, T : Declaration> P.declarationList(
    create: (JSONObject, P) -> T
): Prop<List<T>> =
    sortedList { source -> create(source, this) }

private fun getDocumentation(
    summary: String?,
    remarks: String? = null,
    dpdata: DpData? = null,
    preconditions: List<String>? = null,
    postconditions: List<String>? = null,
    parameters: List<IParameter>? = null,
    typeparameters: List<TypeParameter>? = null,
    returns: IReturns? = null,
    value: Value? = null,
    defaultValue: DefaultValue? = null,
    exceptions: List<ExceptionDescription>? = null,
    relatedDemos: List<Demo>? = null,
    seeAlso: List<SeeAlso>? = null,
    additionalDocumentation: String? = null
): String {
    var lines = getDocumentationLines(
        summary = summary,
        remarks = remarks,
        dpdata = dpdata,
        preconditions = preconditions,
        postconditions = postconditions,
        parameters = parameters,
        typeparameters = typeparameters,
        returns = returns,
        value = value,
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
    dpdata: DpData? = null,
    preconditions: List<String>? = null,
    postconditions: List<String>? = null,
    parameters: List<IParameter>? = null,
    typeparameters: List<TypeParameter>? = null,
    returns: IReturns? = null,
    value: Value? = null,
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

        lines.addAll(
            it.replace(". If th", ".\nIf th")
                .split(LINE_DELIMITER)
        )
    }

    dpdata?.let {
        if (lines.isNotEmpty()) {
            lines.add("")
        }

        lines.addAll(it.toDoc())
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

    value?.let {
        if (lines.isNotEmpty()) {
            lines.add("")
        }

        lines.add(it.toDoc())
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
