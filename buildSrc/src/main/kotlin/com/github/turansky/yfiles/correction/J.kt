package com.github.turansky.yfiles.correction

import org.json.JSONArray
import org.json.JSONObject

internal sealed class JKey(val name: String) {
    override fun toString(): String = name
}

internal class JArrayKey(name: String) : JKey(name)
internal class JObjectKey(name: String) : JKey(name)

internal class JStringKey(name: String) : JKey(name)

internal val J_NAMESPACES = JArrayKey("namespaces")
internal val J_TYPES = JArrayKey("types")

internal val J_FUNCTION_SIGNATURES = JObjectKey("functionSignatures")

internal val J_ID = JStringKey("id")
internal val J_TYPE_PARAMETERS = JArrayKey("typeparameters")
internal val J_BOUNDS = JArrayKey("bounds")

internal val J_EXTENDS = JStringKey("extends")
internal val J_IMPLEMENTS = JArrayKey("implements")

internal val J_CONSTRUCTORS = JArrayKey("constructors")

internal val J_CONSTANTS = JArrayKey("constants")

internal val J_STATIC_PROPERTIES = JArrayKey("staticProperties")
internal val J_PROPERTIES = JArrayKey("properties")
internal val J_FIELDS = JArrayKey("fields")

internal val J_STATIC_METHODS = JArrayKey("staticMethods")
internal val J_METHODS = JArrayKey("methods")

internal val J_PARAMETERS = JArrayKey("parameters")
internal val J_RETURNS = JObjectKey("returns")
internal val J_DOC = JStringKey("doc")

internal val J_NAME = JStringKey("name")
internal val J_TYPE = JStringKey("type")
internal val J_SIGNATURE = JStringKey("signature")
internal val J_MODIFIERS = JArrayKey("modifiers")
internal val J_SUMMARY = JStringKey("summary")

internal val J_DEFAULT = JObjectKey("y.default")
internal val J_VALUE = JStringKey("value")

internal val J_DP_DATA = JObjectKey("dpdata")
internal val J_DOMAIN = JObjectKey("domain")
internal val J_VALUES = JObjectKey("values")

internal fun JSONObject.has(key: JKey) = has(key.name)

internal fun JSONObject.getJSONArray(key: JArrayKey): JSONArray = getJSONArray(key.name)

internal fun JSONObject.getJSONObject(key: JObjectKey): JSONObject = getJSONObject(key.name)

internal fun JSONObject.getString(key: JStringKey): String = getString(key.name)

internal fun JSONObject.optString(key: JStringKey): String? = optString(key.name)

internal fun JSONObject.remove(key: JKey): Any? = remove(key.name)

internal fun JSONObject.put(key: JStringKey, value: String) {
    put(key.name, value)
}

internal fun JSONObject.put(key: JArrayKey, value: Array<*>) {
    put(key.name, value)
}

internal fun JSONObject.put(key: JArrayKey, value: List<*>) {
    put(key.name, value)
}

internal fun JSONObject.put(key: JArrayKey, value: JSONArray) {
    put(key.name, value)
}
