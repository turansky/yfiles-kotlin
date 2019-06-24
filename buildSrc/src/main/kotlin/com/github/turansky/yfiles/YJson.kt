package com.github.turansky.yfiles

import org.json.JSONObject

internal fun JSONObject.types(): Sequence<JSONObject> =
    jsequence("namespaces")
        .optionalArray("namespaces")
        .jsequence("types")

internal fun JSONObject.addStandardGeneric() {
    put(
        "typeparameters", jArray(
            jObject("name" to "T")
        )
    )
}

internal fun JSONObject.allMethods(vararg methodNames: String): Sequence<JSONObject> =
    (types().optionalArray("methods") + types().optionalArray("staticMethods"))
        .filter { it.getString("name") in methodNames }

internal fun JSONObject.jsequence(name: String): Sequence<JSONObject> =
    getJSONArray(name)
        .asSequence()
        .map { it as JSONObject }

internal fun Sequence<JSONObject>.jsequence(name: String): Sequence<JSONObject> =
    flatMap { it.jsequence(name) }

internal fun JSONObject.optionalArray(name: String): Sequence<JSONObject> =
    if (has(name)) {
        jsequence(name)
    } else {
        emptySequence()
    }

internal fun Sequence<JSONObject>.optionalArray(name: String): Sequence<JSONObject> =
    filter { it.has(name) }
        .jsequence(name)

internal val JSONObject.typeParameter: JSONObject
    get() {
        val typeNames = setOf("type", "tType", "itemType")
        return jsequence("parameters")
            .first { it.getString("name") in typeNames }
    }

internal fun JSONObject.parameter(name: String): JSONObject {
    return jsequence("parameters")
        .first { it.getString("name") == name }
}


internal val JSONObject.firstParameter: JSONObject
    get() = getJSONArray("parameters")
        .get(0) as JSONObject

internal fun JSONObject.addGeneric(generic: String) {
    val type = getString("type")
    put("type", "$type<$generic>")
}