package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jArray
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.objects
import org.json.JSONObject

internal fun JSONObject.staticMethod(name: String): JSONObject =
    get(J_STATIC_METHODS)
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
    val result = get(J_METHODS)
        .objects { it[J_NAME] == methodName }
        .flatMap {
            it[J_PARAMETERS]
                .objects { it[J_NAME] == parameterName }
                .filter(parameterFilter)
        }

    require(result.isNotEmpty())
    { "No method parameters found for object: $this, method: $methodName, parameter: $parameterName" }

    return result
}

internal fun JSONObject.method(methodName: String) =
    get(J_METHODS)
        .firstWithName(methodName)

internal fun JSONObject.property(name: String): JSONObject =
    get(J_PROPERTIES)
        .firstWithName(name)

internal fun JSONObject.addProperty(
    propertyName: String,
    type: String
) {
    get(J_PROPERTIES)
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
    val modifiers = get(J_MODIFIERS)
    val index = modifiers.indexOf(modifier)

    require((index == -1) == value)

    if (value) {
        modifiers.put(modifier)
    } else {
        modifiers.remove(index)
    }
}

internal fun JSONObject.setSingleTypeParameter(
    name: String = "T",
    bound: String? = null
) {
    set(
        J_TYPE_PARAMETERS,
        jArray(
            typeParameter(name, bound)
        )
    )
}

internal fun JSONObject.addFirstTypeParameter(
    name: String,
    bound: String? = null
) {
    val parameters = get(J_TYPE_PARAMETERS)
        .toMutableList()

    parameters.add(0, typeParameter(name, bound))

    set(J_TYPE_PARAMETERS, parameters.toList())
}

internal fun JSONObject.setTypeParameters(
    name1: String,
    name2: String
) {
    set(
        J_TYPE_PARAMETERS,
        jArray(
            typeParameter(name1),
            typeParameter(name2)
        )
    )
}

internal fun typeParameter(
    name: String,
    bound: String? = null
): JSONObject =
    jObject(J_NAME to name).apply {
        if (bound != null) {
            set(J_BOUNDS, arrayOf(bound))
        }
    }

internal fun JSONObject.jsequence(key: JArrayKey): Sequence<JSONObject> =
    getJSONArray(key.name)
        .asSequence()
        .map { it as JSONObject }

internal fun JSONObject.optJsequence(key: JArrayKey): Sequence<JSONObject> =
    if (has(key)) {
        jsequence(key)
    } else {
        emptySequence()
    }

internal fun Sequence<JSONObject>.jsequence(key: JArrayKey): Sequence<JSONObject> =
    flatMap { it.jsequence(key) }

internal fun JSONObject.optionalArray(key: JArrayKey): Sequence<JSONObject> =
    if (has(key)) {
        jsequence(key)
    } else {
        emptySequence()
    }

internal fun Sequence<JSONObject>.optionalArray(key: JArrayKey): Sequence<JSONObject> =
    filter { it.has(key) }
        .jsequence(key)

internal val JSONObject.typeParameter: JSONObject
    get() {
        val typeNames = setOf("type", "tType", "itemType")
        return jsequence(J_PARAMETERS)
            .first { it[J_NAME] in typeNames }
    }

internal fun JSONObject.parameter(name: String): JSONObject {
    return jsequence(J_PARAMETERS)
        .first { it[J_NAME] == name }
}

internal val JSONObject.firstParameter: JSONObject
    get() = get(J_PARAMETERS)
        .get(0) as JSONObject

internal val JSONObject.secondParameter: JSONObject
    get() = get(J_PARAMETERS)
        .get(1) as JSONObject

internal fun JSONObject.addGeneric(generic: String) {
    val type = get(J_TYPE)
    set(J_TYPE, "$type<$generic>")
}

internal fun JSONObject.addExtendsGeneric(generic: String) {
    val type = get(J_EXTENDS)
    set(J_EXTENDS, "$type<$generic>")
}
