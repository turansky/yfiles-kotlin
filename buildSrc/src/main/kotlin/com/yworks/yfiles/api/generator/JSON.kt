package com.yworks.yfiles.api.generator

import org.json.JSONArray
import org.json.JSONObject


internal fun jArray(vararg items: JSONObject): JSONArray {
    return JSONArray(items.toList())
}

internal fun jObject(vararg items: Pair<String, Any>): JSONObject {
    return JSONObject(mapOf(*items))
}