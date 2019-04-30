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
        .onEach { it.correctProperties() }
        .forEach { it.correctMethods() }
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
    if (propertyName.endsWith("Count")) {
        return INT
    }

    if (propertyName.endsWith("Cost")) {
        return DOUBLE
    }

    if (className == "AffineLine" && (propertyName == "a" || propertyName == "b")) {
        return DOUBLE
    }

    return when (propertyName) {
        in INT_PROPERTIES -> INT
        in DOUBLE_PROPERTIES -> DOUBLE
        else -> throw IllegalStateException("Unexpected $className.$propertyName")
    }
}

private fun JSONObject.correctMethods() {
    if (!has("methods")) {
        return
    }

    val className = getString("name")
    getJSONArray("methods")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("returns") }
        .forEach {
            val returns = it.getJSONObject("returns")
            if (returns.getString("type") == JS_NUMBER) {
                returns.put("type", getReturnType(className, it.getString("name")))
            }
        }
}

private fun getReturnType(className: String, methodName: String): String {
    if (methodName.endsWith("Count")) {
        return INT
    }

    if (methodName.endsWith("Cost") || methodName.endsWith("Costs")) {
        return DOUBLE
    }

    if (className == "YVector" || className == "LineSegment" && methodName == "length") {
        return DOUBLE
    }

    return when (methodName) {
        in INT_METHODS -> INT
        in DOUBLE_METHODS -> DOUBLE
        else -> {
            println("Unexpected $className.$methodName")
            DOUBLE
        }
    }
}

