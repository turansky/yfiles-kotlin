package com.yworks.yfiles.api.generator

import org.json.JSONObject

private val INT = "Int"
private val DOUBLE = "Double"

internal fun correctNumbers(source: JSONObject) {
    source.getJSONArray("namespaces")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("namespaces") }
        .flatMap { it.getJSONArray("namespaces").asSequence() }
        .map { it as JSONObject }
        .flatMap { it.getJSONArray("types").asSequence() }
        .map { it as JSONObject }
        .forEach { it.correctProperties() }
}

private fun JSONObject.correctProperties() {
    if (!has("properties")) {
        return
    }

    val className = getString("name")
        getJSONArray("properties")
            .asSequence()
            .map { it as JSONObject }
            .filter { it.getString("type") == JS_NUMBER }
            .forEach { it.put("type", getPropertyType(className, it.getString("name"))) }
}

private fun getPropertyType(className: String, propertyName: String): String {
    if (className == "AffineLine" && (propertyName == "a" || propertyName == "b")) {
        return DOUBLE
    }

    return when (propertyName) {
        in INT_PROPERTIES -> INT
        in DOUBLE_PROPERTIES -> DOUBLE
        else -> throw IllegalStateException("Unexpected $className.$propertyName")
    }
}

