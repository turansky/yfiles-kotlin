package com.yworks.yfiles.api.generator

import org.json.JSONObject
import java.util.*
import kotlin.reflect.KProperty

abstract class JsonWrapper(val source: JSONObject)
abstract class JDeclaration : JsonWrapper {
    val id: String by StringDelegate()
    val name: String by StringDelegate()
    protected val modifiers: JModifiers by ModifiersDelegate()

    val summary: String by StringDelegate()
    val remarks: String by StringDelegate()

    val fqn: String
    val nameOfClass: String

    constructor(source: JSONObject) : super(source) {
        this.fqn = id
        this.nameOfClass = fqn.split(".").last()
    }

    constructor(fqn: String, source: JSONObject) : super(source) {
        this.fqn = fqn
        this.nameOfClass = fqn.split(".").last()
    }

    override fun toString(): String {
        throw IllegalStateException("toString() method must be overridden for object " + this)
    }
}

class JAPIRoot(source: JSONObject) : JsonWrapper(source) {
    val namespaces: List<JNamespace> by ArrayDelegate { JNamespace(it) }
    val functionSignatures: Map<String, JSignature> by MapDelegate { name, source -> JSignature(name, source) }
}

class JNamespace(source: JSONObject) : JsonWrapper(source) {
    companion object {
        fun parseType(source: JSONObject): JType {
            val group = source.getString("group")
            return when (group) {
                "class" -> JClass(source)
                "interface" -> JInterface(source)
                "enum" -> JEnum(source)
                else -> throw IllegalArgumentException("Undefined type group '$group'")
            }
        }
    }

    val id: String by StringDelegate()
    val name: String by StringDelegate()

    val namespaces: List<JNamespace> by ArrayDelegate { JNamespace(it) }
    val types: List<JType> by ArrayDelegate { parseType(it) }
}

class JSignature(val fqn: String, source: JSONObject) : JsonWrapper(source) {
    val summary: String by StringDelegate()
    val parameters: List<JSignatureParameter> by ArrayDelegate { JSignatureParameter(it) }
    val typeparameters: List<JTypeParameter> by ArrayDelegate { JTypeParameter(it) }
    val returns: JSignatureReturns? by SignatureReturnsDelegate()
}

class JSignatureParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
    val type: String by TypeDelegate { TypeParser.parse(it) }
    val summary: String by StringDelegate()
}

class JSignatureReturns(source: JSONObject) : JsonWrapper(source) {
    val type: String by StringDelegate()
}

abstract class JType(source: JSONObject) : JDeclaration(source) {
    val constants: List<JConstant> by ArrayDelegate { JConstant(this.fqn, it) }

    val properties: List<JProperty> by ArrayDelegate { JProperty(this.fqn, it) }
    val staticProperties: List<JProperty> by ArrayDelegate { JProperty(this.fqn, it) }

    val methods: List<JMethod> by ArrayDelegate({ JMethod(this.fqn, it) }, { !Hacks.redundantMethod(it) })
    val staticMethods: List<JMethod> by ArrayDelegate({ JMethod(this.fqn, it) }, { !Hacks.redundantMethod(it) })

    val typeparameters: List<JTypeParameter> by ArrayDelegate { JTypeParameter(it) }

    val extends: String? by NullableStringDelegate()
    val implements: List<String> by StringArrayDelegate()

    fun genericParameters(): String {
        return TypeParser.getGenericString(typeparameters)
    }

    fun extendedType(): String? {
        if (Hacks.ignoreExtendedType(fqn)) {
            return null
        }

        val type = extends ?: return null
        return TypeParser.parseType(type)
    }

    fun implementedTypes(): List<String> {
        var types = Hacks.getImplementedTypes(fqn)
        if (types != null) {
            return types
        }

        types = implements.map { TypeParser.parseType(it) }
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
    val protected = modifiers.protected

    val modificator: String = when {
        protected -> "protected "
        else -> ""
    }

    override fun toString(): String {
        return "${modificator}constructor(${parametersString()})"
    }
}

class JConstant(fqn: String, source: JSONObject) : JTypedDeclaration(fqn, source) {
    override fun toString(): String {
        val type = Hacks.correctStaticFieldGeneric(this.type)
        return "val $name: $type = definedExternally"
    }
}

class JProperty(fqn: String, source: JSONObject) : JTypedDeclaration(fqn, source) {
    val static = modifiers.static
    val protected = modifiers.protected
    val getterSetter = !modifiers.readOnly

    val abstract = modifiers.abstract

    override fun toString(): String {
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
}

class JMethod(fqn: String, source: JSONObject) : JMethodBase(fqn, source) {
    val abstract = modifiers.abstract
    val static = modifiers.static
    val protected = modifiers.protected

    val typeparameters: List<JTypeParameter> by ArrayDelegate { JTypeParameter(it) }
    val returns: JReturns? by ReturnsDelegate()

    val generics: String
        get() = Hacks.getFunctionGenerics(fqn, name) ?: TypeParser.getGenericString(typeparameters)

    private fun modificator(canBeOpen: Boolean = true): String {
        val classRegistry: ClassRegistry = ClassRegistry.instance

        // TODO: add abstract modificator if needed
        if (classRegistry.functionOverriden(fqn, name)) {
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
        val returnType = returns?.type ?: Types.UNIT
        val body = if (abstract) "" else " = definedExternally"
        return "${modificator()}fun $generics$name(${parametersString()}): $returnType$body"
    }
}

abstract class JMethodBase(fqn: String, source: JSONObject) : JDeclaration(fqn, source) {
    val parameters: List<JParameter> by ArrayDelegate({ JParameter(this, it) }, { !it.artificial })

    protected fun parametersString(checkOverriding: Boolean = true): String {
        val overridden = checkOverriding && ClassRegistry.instance.functionOverriden(fqn, name)
        return parameters.map {
            if (overridden) {
                if (it.optional && !overridden) {
                    println("Optional - ${fqn}.${name} -  ${it.getCorrectedName()}")
                }
            }

            val body = if (it.optional && !overridden) " = definedExternally" else ""
            "${it.getCorrectedName()}: ${Hacks.getParameterType(this, it) ?: it.type}" + body

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

class JParameter(private val method: JMethodBase, source: JSONObject) : JsonWrapper(source) {
    private val name: String by StringDelegate()
    val artificial: Boolean by BooleanDelegate()
    val type: String by TypeDelegate { TypeParser.parse(it) }
    val summary: String? by NullableStringDelegate()
    val optional: Boolean by BooleanDelegate()

    fun getCorrectedName(): String {
        return Hacks.fixParameterName(method, name) ?: name
    }
}

class JTypeParameter(source: JSONObject) : JsonWrapper(source) {
    val name: String by StringDelegate()
}

class JReturns(val type: String, source: JSONObject) : JsonWrapper(source)

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
        for (i in 0..length - 1) {
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
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): JModifiers {
        return JModifiers(StringArrayDelegate.value(thisRef, property))
    }
}

private class SignatureReturnsDelegate {
    operator fun getValue(thisRef: JsonWrapper, property: KProperty<*>): JSignatureReturns? {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) {
            JSignatureReturns(source.getJSONObject(key))
        } else {
            null
        }
    }
}

private class ReturnsDelegate {
    operator fun getValue(thisRef: JMethod, property: KProperty<*>): JReturns? {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) {
            val data = source.getJSONObject(key)
            val type = Hacks.getReturnType(thisRef)
                    ?: TypeParser.parse(data.getString("type"))
            JReturns(type, data)
        } else {
            null
        }
    }
}
