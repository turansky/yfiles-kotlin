package com.github.turansky.yfiles

import org.json.JSONObject

internal fun JSONObject.types(): Sequence<JSONObject> {
    return getJSONArray("namespaces")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("namespaces") }
        .flatMap { it.getJSONArray("namespaces").asSequence() }
        .map { it as JSONObject }
        .flatMap { it.getJSONArray("types").asSequence() }
        .map { it as JSONObject }
}

internal fun JSONObject.addStandardGeneric() {
    put(
        "typeparameters", jArray(
            jObject("name" to "T")
        )
    )
}