package com.yworks.yfiles.api.generator

import org.json.JSONObject

internal fun correctNumbers(source: JSONObject) {
    val types = source.getJSONArray("namespaces")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("namespaces") }
        .flatMap { it.getJSONArray("namespaces").asSequence() }
        .map { it as JSONObject }
        .flatMap { it.getJSONArray("types").asSequence() }
        .map { it as JSONObject }
        .toList()

    val numberProperties = types
        .asSequence()
        .flatMap { it.correctProperties() }
        .toSet()
}

private fun JSONObject.correctProperties() =
    if (has("properties")) {
        getJSONArray("properties")
            .asSequence()
            .map { it as JSONObject }
            .filter { it.getString("type") == JS_NUMBER }
            .map { it.getString("name") }
    } else {
        emptySequence()
    }