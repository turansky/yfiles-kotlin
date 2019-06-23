package com.github.turansky.yfiles

import org.json.JSONObject

internal fun JSONObject.types(): Sequence<JSONObject> =
    getJSONArray("namespaces")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("namespaces") }
        .flatMap { it.getJSONArray("namespaces").asSequence() }
        .map { it as JSONObject }
        .flatMap { it.getJSONArray("types").asSequence() }
        .map { it as JSONObject }

internal fun JSONObject.addStandardGeneric() {
    put(
        "typeparameters", jArray(
            jObject("name" to "T")
        )
    )
}

internal fun JSONObject.allMethods(vararg methodNames: String): Sequence<JSONObject> =
    types()
        .filter { it.has("methods") }
        .flatMap { it.getJSONArray("methods").asSequence() }
        .map { it as JSONObject }
        .filter { it.getString("name") in methodNames }

internal val JSONObject.typeParameter: JSONObject
    get() {
        val typeNames = setOf("type", "tType", "itemType")
        return getJSONArray("parameters")
            .asSequence()
            .map { it as JSONObject }
            .first { it.getString("name") in typeNames }
    }

internal fun JSONObject.parameter(name: String): JSONObject {
    return getJSONArray("parameters")
        .asSequence()
        .map { it as JSONObject }
        .first { it.getString("name") == name }
}


internal val JSONObject.firstParameter: JSONObject
    get() = getJSONArray("parameters")
        .get(0) as JSONObject

internal fun JSONObject.addGeneric(generic: String) {
    val type = getString("type")
    put("type", "$type<$generic>")
}