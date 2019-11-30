package com.github.turansky.yfiles.correction

import org.json.JSONArray
import org.json.JSONObject

internal sealed class JKey(val name: String) {
    override fun toString(): String = name
}

internal sealed class JArrayKey(name: String) : JKey(name)
internal sealed class JObjectKey(name: String) : JKey(name)

internal sealed class JStringKey(name: String) : JKey(name)

internal object J_NAMESPACES : JArrayKey("namespaces")
internal object J_TYPES : JArrayKey("types")

internal object J_FUNCTION_SIGNATURES : JObjectKey("functionSignatures")

internal object J_ID : JStringKey("id")
internal object J_TYPE_PARAMETERS : JArrayKey("typeparameters")
internal object J_BOUNDS : JArrayKey("bounds")

internal object J_EXTENDS : JStringKey("extends")
internal object J_IMPLEMENTS : JArrayKey("implements")

internal object J_CONSTRUCTORS : JArrayKey("constructors")

internal object J_CONSTANTS : JArrayKey("constants")

internal object J_STATIC_PROPERTIES : JArrayKey("staticProperties")
internal object J_PROPERTIES : JArrayKey("properties")
internal object J_FIELDS : JArrayKey("fields")

internal object J_STATIC_METHODS : JArrayKey("staticMethods")
internal object J_METHODS : JArrayKey("methods")

internal object J_PARAMETERS : JArrayKey("parameters")
internal object J_RETURNS : JObjectKey("returns")
internal object J_DOC : JStringKey("doc")

internal object J_NAME : JStringKey("name")
internal object J_ES6_NAME : JStringKey("es6name")
internal object J_TYPE : JStringKey("type")
internal object J_SIGNATURE : JStringKey("signature")
internal object J_MODIFIERS : JArrayKey("modifiers")
internal object J_SUMMARY : JStringKey("summary")

internal object J_DEFAULT : JObjectKey("y.default")
internal object J_VALUE : JStringKey("value")

internal object J_DP_DATA : JObjectKey("dpdata")
internal object J_DOMAIN : JObjectKey("domain")
internal object J_VALUES : JObjectKey("values")

internal fun JSONObject.has(key: JKey) = has(key.name)

internal operator fun JSONObject.get(key: JArrayKey): JSONArray = getJSONArray(key.name)

internal operator fun JSONObject.get(key: JObjectKey): JSONObject = getJSONObject(key.name)

internal operator fun JSONObject.get(key: JStringKey): String = getString(key.name)

internal fun JSONObject.optString(key: JStringKey): String? = optString(key.name)

internal fun JSONObject.remove(key: JKey): Any? = remove(key.name)

internal operator fun JSONObject.set(key: JStringKey, value: String) {
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
