package com.github.turansky.yfiles.correction

import org.json.JSONArray
import org.json.JSONObject

internal sealed class JKey(val name: String) {
    override fun toString(): String = name
}

internal sealed class JArrayKey(name: String) : JKey(name)
internal sealed class JObjectKey(name: String) : JKey(name)

internal sealed class JStringKey(name: String) : JKey(name)

internal object NAMESPACES : JArrayKey("namespaces")
internal object TYPES : JArrayKey("types")

internal object FUNCTION_SIGNATURES : JObjectKey("functionSignatures")

internal object ID : JStringKey("id")
internal object TYPE_PARAMETERS : JArrayKey("typeparameters")
internal object BOUNDS : JArrayKey("bounds")

internal object EXTENDS : JStringKey("extends")
internal object IMPLEMENTS : JArrayKey("implements")

internal object CONSTRUCTORS : JArrayKey("constructors")

internal object CONSTANTS : JArrayKey("constants")

internal object STATIC_PROPERTIES : JArrayKey("staticProperties")
internal object PROPERTIES : JArrayKey("properties")
internal object FIELDS : JArrayKey("fields")

internal object STATIC_METHODS : JArrayKey("staticMethods")
internal object METHODS : JArrayKey("methods")

internal object EVENTS : JArrayKey("events")

internal object PARAMETERS : JArrayKey("parameters")
internal object RETURNS : JObjectKey("returns")
internal object DOC : JStringKey("doc")

internal object NAME : JStringKey("name")
internal object ES6_NAME : JStringKey("es6name")
internal object TYPE : JStringKey("type")
internal object SIGNATURE : JStringKey("signature")
internal object MODIFIERS : JArrayKey("modifiers")
internal object SUMMARY : JStringKey("summary")

internal object DEFAULT : JObjectKey("y.default")
internal object VALUE : JStringKey("value")

internal object DP_DATA : JObjectKey("dpdata")
internal object DOMAIN : JObjectKey("domain")
internal object VALUES : JObjectKey("values")

internal fun JSONObject.has(key: JKey) = has(key.name)

internal operator fun JSONObject.get(key: JArrayKey): JSONArray = getJSONArray(key.name)

internal fun JSONObject.opt(key: JArrayKey): JSONArray? = optJSONArray(key.name)

internal operator fun JSONObject.get(key: JObjectKey): JSONObject = getJSONObject(key.name)

internal operator fun JSONObject.get(key: JStringKey): String = getString(key.name)

internal fun JSONObject.opt(key: JStringKey): String? = optString(key.name, null)

internal fun JSONObject.remove(key: JKey): Any? = remove(key.name)

internal operator fun JSONObject.set(key: JStringKey, value: String) {
    put(key.name, value)
}

internal operator fun JSONObject.set(key: JArrayKey, value: Array<*>) {
    put(key.name, value)
}

internal operator fun JSONObject.set(key: JArrayKey, value: List<*>) {
    put(key.name, value)
}

internal operator fun JSONObject.set(key: JArrayKey, value: JSONArray) {
    put(key.name, value)
}
