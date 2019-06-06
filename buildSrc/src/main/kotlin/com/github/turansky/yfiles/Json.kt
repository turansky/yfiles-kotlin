package com.github.turansky.yfiles

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

internal fun JSONArray.objects(predicate: (JSONObject) -> Boolean): Iterable<JSONObject> {
    return (0 until this.length())
        .map(this::getJSONObject)
        .filter(predicate)
}