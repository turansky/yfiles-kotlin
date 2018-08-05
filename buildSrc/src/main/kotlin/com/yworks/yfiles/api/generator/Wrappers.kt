package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.JavaTypes.VOID
import org.json.JSONObject
import java.util.*
import kotlin.reflect.KProperty

internal abstract class JsonWrapper(val source: JSONObject) {
    protected open fun toKotlinCode(): String {
        throw IllegalStateException("toKotlinCode() method must be overridden")
    }

    protected open fun toJavaCode(): String {
        throw IllegalStateException("toJavaCode() method must be overridden")
    }

    fun toCode(programmingLanguage: ProgrammingLanguage): String {
        return when (programmingLanguage) {
            ProgrammingLanguage.KOTLIN -> toKotlinCode()
            ProgrammingLanguage.JAVA -> toJavaCode()
        }
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

    val fqn: String
    val nameOfClass: String

    constructor(source: JSONObject) : super(source) {
        this.fqn = fixPackage(id)
        this.nameOfClass = fqn.split(".").last()
    }

    constructor(fqn: String, source: JSONObject) : super(source) {
        this.fqn = fixPackage(fqn)
        this.nameOfClass = fqn.split(".").last()
    }
}

internal class ApiRoot(source: JSONObject) : JsonWrapper(source) {
    val namespaces: List<Namespace> by ArrayDelegate(::Namespace)
    val functionSignatures: Map<String, FunctionSignature> by MapDelegate { name, source -> FunctionSignature(name, source) }
}

internal class Namespace(source: JSONObject) : JsonWrapper(source) {
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

    val id: String by StringDelegate()
    val name: String by StringDelegate()

    val namespaces: List<Namespace> by ArrayDelegate { Namespace(it) }
    val types: List<Type> by ArrayDelegate { parseType(it) }
}

internal class FunctionSignature(fqn: String, source: JSONObject) : JsonWrapper(source) {
    val fqn = fixPackage(fqn)
    val summary: String by StringDelegate()
    val parameters: List<SignatureParameter> by ArrayDelegate(::SignatureParameter)
    val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)
    val returns: SignatureReturns? by SignatureReturnsDelegate()
}

internal class SignatureParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    val type: String by TypeDelegate { TypeParser.parseType(it) }
    val summary: String by StringDelegate()

    override fun toKotlinCode(): String {
        return "$name: $type"
    }

    override fun toJavaCode(): String {
        return "$type $name"
    }
}

internal class SignatureReturns(source: JSONObject) : JsonWrapper(source) {
    val type: String by TypeDelegate { TypeParser.parseType(it) }
}

internal abstract class Type(source: JSONObject) : Declaration(source) {
    val modules: List<IModule> by ArrayDelegate(::Module)
    val es6Module: String by StringDelegate()
    val constants: List<Constant> by ArrayDelegate { Constant(this.fqn, it) }

    val properties: List<Property> by ArrayDelegate { Property(this.fqn, it) }
    val staticProperties: List<Property> by ArrayDelegate { Property(this.fqn, it) }

    val methods: List<Method> by ArrayDelegate({ Method(this.fqn, it) }, { !Hacks.redundantMethod(it) })
    val staticMethods: List<Method> by ArrayDelegate({ Method(this.fqn, it) }, { !Hacks.redundantMethod(it) })

    val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)

    private val extends: String? by NullableStringDelegate()
    private val implements: List<String> by StringArrayDelegate()

    fun genericParameters(): String {
        return TypeParser.getGenericString(typeparameters)
    }

    fun extendedType(): String? {
        val type = extends ?: return null
        return TypeParser.parseType(type)
    }

    fun implementedTypes(): List<String> {
        val types = implements.map { TypeParser.parseType(it) }
        return MixinHacks.getImplementedTypes(fqn, types)
    }
}

internal class Class(source: JSONObject) : Type(source) {
    val final = modifiers.final
    val open = !final
    val abstract = modifiers.abstract

    val kotlinModificator = when {
        abstract -> "abstract" // no such cases (JS specific?)
        open -> "open"
        else -> ""
    }

    val javaModificator = when {
        abstract -> "abstract" // no such cases (JS specific?)
        final -> "final"
        else -> ""
    }

    val constructors: List<Constructor> by ArrayDelegate { Constructor(this.fqn, it) }
}

internal class Modifiers(flags: List<String>) {
    val static = flags.contains("static")
    val final = flags.contains("final")
    val readOnly = flags.contains("ro")
    val abstract = flags.contains("abstract")
    val protected = flags.contains("protected")
}

internal class Interface(source: JSONObject) : Type(source)
internal class Enum(source: JSONObject) : Type(source)

private class Module(source: JSONObject) : JsonWrapper(source), IModule {
    override val text: String? by NullableStringDelegate()
    override val moduleId: String? by NullableStringDelegate()
}

internal abstract class TypedDeclaration(fqn: String, source: JSONObject) : Declaration(fqn, source) {
    private val signature: String? by NullableStringDelegate()
    protected val type: String by TypeDelegate { TypeParser.parse(it, signature) }
}

internal class Constructor(fqn: String, source: JSONObject) : MethodBase(fqn, source) {
    val protected = modifiers.protected

    override fun toKotlinCode(): String {
        val modificator: String = when {
            protected -> "protected"
            else -> ""
        }

        return "${modificator} constructor(${kotlinParametersString()})"
    }

    override fun toJavaCode(): String {
        val modificator: String = when {
            protected -> "protected"
            else -> "public"
        }

        return "${modificator} ${nameOfClass}(${javaParametersString()}) {}"
    }
}

internal class Constant(fqn: String, source: JSONObject) : TypedDeclaration(fqn, source) {
    override fun toKotlinCode(): String {
        return "val $name: $type = definedExternally"
    }

    override fun toJavaCode(): String {
        val annotation = "@jsinterop.annotations.JsProperty(name=\"$name\")"
        return "$annotation\npublic static native $type $name();"
    }
}

internal class Property(fqn: String, source: JSONObject) : TypedDeclaration(fqn, source) {
    val static = modifiers.static
    val protected = modifiers.protected
    val getterSetter = !modifiers.readOnly

    val abstract = modifiers.abstract

    override fun toKotlinCode(): String {
        var str = ""
        val classRegistry: ClassRegistry = ClassRegistry.instance

        if (classRegistry.propertyOverriden(fqn, name)) {
            str += "override "
        } else {
            if (protected) {
                str += "protected "
            }

            str += when {
                abstract -> "abstract "
                !static && !classRegistry.isFinalClass(fqn) -> "open "
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

    override fun toJavaCode(): String {
        val classRegistry: ClassRegistry = ClassRegistry.instance

        val override = if (classRegistry.propertyOverriden(fqn, name)) {
            "@Override\n"
        } else {
            ""
        }

        val modificator = if (protected) {
            "protected "
        } else {
            "public "
        } + if (abstract) {
            "abstract "
        } else {
            ""
        } + if (classRegistry.isInterface(fqn)) {
            ""
        } else {
            "native "
        }

        val annotation = "@jsinterop.annotations.JsProperty(name=\"$name\")"
        val getterName = if (type != "boolean") {
            "get" + name.capitalize()
        } else {
            if (name.startsWith("is")) {
                name
            } else {
                "is" + name.capitalize()
            }
        }

        var str = annotation + "\n" + "$override $modificator $type $getterName();"

        if (getterSetter) {
            val setterName = "set" + name.capitalize()
            str += annotation + "\n" + "$modificator void $setterName($type value);"
        }

        return str
    }
}

internal class Method(fqn: String, source: JSONObject) : MethodBase(fqn, source) {
    val abstract = modifiers.abstract
    val static = modifiers.static
    val protected = modifiers.protected

    val typeparameters: List<TypeParameter> by ArrayDelegate(::TypeParameter)
    val returns: Returns? by ReturnsDelegate()

    val generics: String
        get() = TypeParser.getGenericString(typeparameters)

    private fun kotlinModificator(): String {
        val classRegistry: ClassRegistry = ClassRegistry.instance

        // TODO: add abstract modificator if needed
        if (classRegistry.functionOverriden(fqn, name)) {
            return "override "
        }

        val result = when {
            abstract -> "abstract "
            !static && !classRegistry.isFinalClass(fqn) -> "open "
            else -> ""
        }
        return result + when {
            protected -> "protected "
            else -> ""
        }
    }

    private fun javaModificator(): String {
        val classRegistry: ClassRegistry = ClassRegistry.instance

        val override = if (classRegistry.functionOverriden(fqn, name)) {
            "@Override\n"
        } else {
            ""
        }

        val mofificators = mutableListOf<String>()

        if (!classRegistry.isInterface(fqn)) {
            if (abstract) {
                mofificators.add("abstract ")
            } else {
                mofificators.add("native")
            }

            if (protected) {
                mofificators.add("protected")
            } else {
                mofificators.add("public")
            }
        }

        return override + mofificators.joinToString(" ") + " "
    }

    override fun toKotlinCode(): String {
        val returnType = returns?.type
        val returnSignature = if (abstract) {
            if (returnType != null) {
                ":" + returnType
            } else {
                ""
            }
        } else {
            ":" + (returnType ?: KotlinTypes.UNIT) + " = definedExternally"
        }
        return "${kotlinModificator()}fun $generics$name(${kotlinParametersString()})$returnSignature"
    }

    override fun toJavaCode(): String {
        val returnType = returns?.type ?: VOID
        return "${javaModificator()} $generics $returnType $name(${javaParametersString()});"
    }
}

// TODO: support artificial parameters
internal abstract class MethodBase(fqn: String, source: JSONObject) : Declaration(fqn, source) {
    val parameters: List<Parameter> by ArrayDelegate({ Parameter(it) }, { !it.modifiers.artificial })
    val options: Boolean by BooleanDelegate()

    protected fun kotlinParametersString(checkOverriding: Boolean = true): String {
        val overridden = checkOverriding && ClassRegistry.instance.functionOverriden(fqn, name)
        return parameters.map {
            val modifiers = if (it.modifiers.vararg) "vararg " else ""
            val body = if (it.modifiers.optional && !overridden) " = definedExternally" else ""
            "$modifiers ${it.name}: ${it.type}" + body
        }.joinToString(", ")
    }

    protected fun javaParametersString(): String {
        return parameters.map {
            val name = if (it.name != "synchronized") it.name else "synchronized1" // TODO: find better name

            val modifiers = if (it.modifiers.vararg) "..." else ""
            "${it.type} $modifiers${name}"
        }.joinToString(", ")
    }

    override fun hashCode(): Int {
        return Objects.hash(fqn, name, kotlinParametersString(false))
    }

    override fun equals(other: Any?): Boolean {
        return other is MethodBase
                && Objects.equals(fqn, other.fqn)
                && Objects.equals(name, other.name)
                && Objects.equals(kotlinParametersString(false), other.kotlinParametersString(false))
    }
}

internal class ParameterModifiers(flags: List<String>) {
    val artificial = flags.contains("artificial")
    val vararg = flags.contains("vararg")
    val optional = flags.contains("optional")
    val conversion = flags.contains("conversion")
}

internal class Parameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    private val signature: String? by NullableStringDelegate()
    val type: String by TypeDelegate { TypeParser.parse(it, signature) }
    val summary: String? by NullableStringDelegate()
    val modifiers: ParameterModifiers by ParameterModifiersDelegate()
}

internal class TypeParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
}

internal class Returns(source: JSONObject) : JsonWrapper(source) {
    private val signature: String? by NullableStringDelegate()
    val type: String by TypeDelegate { TypeParser.parse(it, signature) }
}

private class ArrayDelegate<T> {

    private val transform: (JSONObject) -> T
    private val filter: (T) -> Boolean

    constructor(transform: (JSONObject) -> T) : this(transform, { true })

    constructor(transform: (JSONObject) -> T, filter: (T) -> Boolean) {
        this.transform = transform
        this.filter = filter
    }

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
        for (i in 0 until length) {
            val item = transform(array.getJSONObject(i))
            if (filter(item)) {
                list.add(item)
            }
        }
        return list.toList()
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
