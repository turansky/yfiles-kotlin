package com.github.turansky.yfiles.json

import com.github.turansky.yfiles.correction.JKey
import com.github.turansky.yfiles.correction.NAME
import com.github.turansky.yfiles.correction.get
import com.github.turansky.yfiles.correction.remove
import org.json.JSONArray
import org.json.JSONObject


internal fun jArray(vararg items: JSONObject): JSONArray {
    return JSONArray(items.toList())
}

internal fun jObject(vararg items: Pair<JKey, Any>): JSONObject {
    return JSONObject(
        items.associate { (key, value) -> key.name to value }
    )
}

internal fun JSONArray.first(predicate: (JSONObject) -> Boolean): JSONObject {
    return (0 until this.length())
        .map(this::getJSONObject)
        .filter(predicate)
        .first()
}

internal operator fun JSONArray.get(name: String): JSONObject =
    first { it[NAME] == name }

internal fun JSONArray.objects(predicate: (JSONObject) -> Boolean): Iterable<JSONObject> {
    return (0 until this.length())
        .map(this::getJSONObject)
        .filter(predicate)
}

internal fun JSONObject.strictRemove(key: JKey) {
    requireNotNull(remove(key))
}

internal fun JSONObject.strictRemove(name: String) {
    requireNotNull(remove(name))
}

internal fun JSONArray.strictRemove(index: Int) {
    requireNotNull(remove(index))
}

internal fun JSONArray.removeItem(item: JSONObject) {
    requireNotNull(remove(indexOf(item)))
}

internal fun JSONArray.removeItem(item: String) {
    requireNotNull(remove(indexOf(item)))
}
