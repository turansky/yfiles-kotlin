package com.github.turansky.yfiles

import org.json.JSONObject

internal class Source(api: JSONObject) {
    private val types = api
        .jsequence("namespaces")
        .optionalArray("namespaces")
        .jsequence("types")

    private val typeMap = types.associateBy { it.getString("id") }

    fun types() = types.asSequence()
    fun type(className: String) = typeMap.getValue(className)

    fun allMethods(vararg methodNames: String): Sequence<JSONObject> =
        types.asSequence()
            .map { it.optionalArray("methods") + it.optionalArray("staticMethods") }
            .flatMap { it.asSequence() }
            .filter { it.getString("name") in methodNames }
}