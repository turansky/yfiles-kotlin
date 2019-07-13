package com.github.turansky.yfiles.json

import com.github.turansky.yfiles.correction.J_NAME
import org.json.JSONArray
import org.json.JSONObject


internal fun jArray(vararg items: JSONObject): JSONArray {
    return JSONArray(items.toList())
}

internal fun jObject(vararg items: Pair<String, Any>): JSONObject {
    return JSONObject(mapOf(*items))
}

internal fun JSONArray.first(predicate: (JSONObject) -> Boolean): JSONObject {
    return (0 until this.length())
        .map(this::getJSONObject)
        .filter(predicate)
        .first()
}

internal fun JSONArray.firstWithName(name: String): JSONObject =
    first { it.getString(J_NAME) == name }

internal fun JSONArray.objects(predicate: (JSONObject) -> Boolean): Iterable<JSONObject> {
    return (0 until this.length())
        .map(this::getJSONObject)
        .filter(predicate)
}

internal fun JSONObject.strictRemove(key: String) {
    requireNotNull(remove(key))
}

internal fun JSONArray.strictRemove(index: Int) {
    requireNotNull(remove(index))
}