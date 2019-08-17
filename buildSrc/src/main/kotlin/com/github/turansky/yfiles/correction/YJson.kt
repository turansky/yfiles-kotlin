package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jArray
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.objects
import org.json.JSONObject

internal fun JSONObject.staticMethod(name: String): JSONObject =
    getJSONArray(J_STATIC_METHODS)
        .firstWithName(name)

internal fun JSONObject.allMethodParameters(): Sequence<JSONObject> =
    (optionalArray(J_METHODS) + optionalArray(J_STATIC_METHODS))
        .optionalArray(J_PARAMETERS)

internal fun JSONObject.methodParameters(
    methodName: String,
    parameterName: String
): Iterable<JSONObject> =
    methodParameters(
        methodName,
        parameterName,
        { true }
    )

internal fun JSONObject.methodParameters(
    methodName: String,
    parameterName: String,
    parameterFilter: (JSONObject) -> Boolean
): Iterable<JSONObject> {
    val result = getJSONArray(J_METHODS)
        .objects { it.getString(J_NAME) == methodName }
        .flatMap {
            it.getJSONArray(J_PARAMETERS)
                .objects { it.getString(J_NAME) == parameterName }
                .filter(parameterFilter)
        }

    require(result.isNotEmpty())
    { "No method parameters found for object: $this, method: $methodName, parameter: $parameterName" }

    return result
}

internal fun JSONObject.property(name: String): JSONObject =
    getJSONArray(J_PROPERTIES)
        .firstWithName(name)

internal fun JSONObject.addProperty(
    propertyName: String,
    type: String
) {
    getJSONArray(J_PROPERTIES)
        .put(
            mapOf(
                J_NAME to propertyName,
                J_MODIFIERS to listOf(PUBLIC, FINAL, RO),
                J_TYPE to type
            )
        )
}

internal fun JSONObject.changeNullability(nullable: Boolean) =
    changeModifier(CANBENULL, nullable)

internal fun JSONObject.changeOptionality(optional: Boolean) =
    changeModifier(OPTIONAL, optional)

private fun JSONObject.changeModifier(modifier: String, value: Boolean) {
    val modifiers = getJSONArray(J_MODIFIERS)
    val index = modifiers.indexOf(modifier)

    require((index == -1) == value)

    if (value) {
        modifiers.put(modifier)
    } else {
        modifiers.remove(index)
    }
}

internal fun JSONObject.setSingleTypeParameter(name: String = "T") {
    put(
        J_TYPE_PARAMETERS,
        jArray(
            jObject(J_NAME to name)
        )
    )
}

internal fun JSONObject.setTypeParameters(
    name1: String,
    name2: String
) {
    put(
        J_TYPE_PARAMETERS,
        jArray(
            jObject(J_NAME to name1),
            jObject(J_NAME to name2)
        )
    )
}

internal fun JSONObject.jsequence(name: String): Sequence<JSONObject> =
    getJSONArray(name)
        .asSequence()
        .map { it as JSONObject }

internal fun JSONObject.optJsequence(name: String): Sequence<JSONObject> =
    if (has(name)) {
        jsequence(name)
    } else {
        emptySequence()
    }

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
        return jsequence(J_PARAMETERS)
            .first { it.getString(J_NAME) in typeNames }
    }

internal fun JSONObject.parameter(name: String): JSONObject {
    return jsequence(J_PARAMETERS)
        .first { it.getString(J_NAME) == name }
}

internal val JSONObject.firstParameter: JSONObject
    get() = getJSONArray(J_PARAMETERS)
        .get(0) as JSONObject

internal val JSONObject.secondParameter: JSONObject
    get() = getJSONArray(J_PARAMETERS)
        .get(1) as JSONObject

internal fun JSONObject.addGeneric(generic: String) {
    val type = getString(J_TYPE)
    put(J_TYPE, "$type<$generic>")
}