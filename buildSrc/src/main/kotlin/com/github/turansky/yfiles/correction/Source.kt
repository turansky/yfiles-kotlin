package com.github.turansky.yfiles.correction

import org.json.JSONObject

internal class Source(private val api: JSONObject) {
    val functionSignatures: JSONObject
        get() = api.getJSONObject(J_FUNCTION_SIGNATURES)

    private val types = api
        .jsequence(J_NAMESPACES)
        .optionalArray(J_NAMESPACES)
        .jsequence(J_TYPES)

    private val typeMap = types.associateBy { it.uid }

    fun types() = types.asSequence()
    fun type(className: String) = typeMap.getValue(className)

    fun allMethods(vararg methodNames: String): Sequence<JSONObject> =
        types.asSequence()
            .map { it.optionalArray(J_METHODS) + it.optionalArray(J_STATIC_METHODS) }
            .flatMap { it.asSequence() }
            .filter { it.getString(J_NAME) in methodNames }

    private val JSONObject.uid: String
        get() = if (has("es6name")) {
            getString("es6name")
        } else {
            getString(J_NAME)
        }
}