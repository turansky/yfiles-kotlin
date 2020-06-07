package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import com.github.turansky.yfiles.json.jArray
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.objects
import org.json.JSONObject

internal fun JSONObject.allMethodParameters(): Sequence<JSONObject> =
    optFlatMap(METHODS)
        .optFlatMap(PARAMETERS)

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
    val result = get(METHODS)
        .objects { it[NAME] == methodName }
        .flatMap {
            it[PARAMETERS]
                .objects { it[NAME] == parameterName }
                .filter(parameterFilter)
        }

    require(result.isNotEmpty())
    { "No method parameters found for object: $this, method: $methodName, parameter: $parameterName" }

    return result
}

internal fun JSONObject.method(name: String) =
    get(METHODS)[name]

internal fun JSONObject.constant(name: String): JSONObject =
    get(CONSTANTS)[name]

internal fun JSONObject.property(name: String): JSONObject =
    get(PROPERTIES)[name]

internal fun JSONObject.addProperty(
    propertyName: String,
    type: String
) {
    get(PROPERTIES)
        .put(
            mapOf(
                NAME to propertyName,
                MODIFIERS to listOf(PUBLIC, FINAL, RO),
                TYPE to type
            )
        )
}

internal fun JSONObject.replaceInType(
    oldValue: String,
    newValue: String
) {
    set(TYPE, get(TYPE).replace(oldValue, newValue))
}

internal fun JSONObject.changeNullability(nullable: Boolean) =
    changeModifier(CANBENULL, nullable)

internal fun JSONObject.changeOptionality(optional: Boolean) =
    changeModifier(OPTIONAL, optional)

private fun JSONObject.changeModifier(modifier: String, value: Boolean) {
    val modifiers = get(MODIFIERS)
    val index = modifiers.indexOf(modifier)

    val hasEffect = (index == -1) == value
    if (CorrectionMode.isNormal()) {
        require(hasEffect)
    } else if (!hasEffect) {
        println("MODIFIER CHANGE FAILED")
    }

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
        TYPE_PARAMETERS,
        jArray(
            typeParameter(name, bound)
        )
    )
}

internal fun JSONObject.addFirstTypeParameter(
    name: String,
    bound: String? = null
) {
    val parameters = get(TYPE_PARAMETERS)
        .toMutableList()

    parameters.add(0, typeParameter(name, bound))

    set(TYPE_PARAMETERS, parameters.toList())
}

internal fun JSONObject.setTypeParameters(
    name1: String,
    name2: String
) {
    set(
        TYPE_PARAMETERS,
        jArray(
            typeParameter(name1),
            typeParameter(name2)
        )
    )
}

internal fun JSONObject.setKeyValueTypeParameters(
    keyName: String = "K",
    valueName: String = "V",
    keyBound: String = JS_OBJECT
) {
    set(
        TYPE_PARAMETERS,
        jArray(
            typeParameter(keyName, keyBound),
            typeParameter(valueName, JS_OBJECT)
        )
    )
}

internal fun typeParameter(
    name: String,
    bound: String? = null
): JSONObject =
    jObject(NAME to name).apply {
        if (bound != null) {
            set(BOUNDS, arrayOf(bound))
        }
    }

internal fun JSONObject.flatMap(key: JArrayKey): Sequence<JSONObject> =
    getJSONArray(key.name)
        .asSequence()
        .map { it as JSONObject }

internal fun JSONObject.optFlatMap(key: JArrayKey): Sequence<JSONObject> =
    if (has(key)) {
        flatMap(key)
    } else {
        emptySequence()
    }

internal fun Sequence<JSONObject>.flatMap(key: JArrayKey): Sequence<JSONObject> =
    flatMap { it.flatMap(key) }

internal fun Sequence<JSONObject>.optFlatMap(key: JArrayKey): Sequence<JSONObject> =
    filter { it.has(key) }
        .flatMap(key)

internal val JSONObject.typeParameter: JSONObject
    get() {
        val typeNames = setOf("type", "tType", "itemType")
        return flatMap(PARAMETERS)
            .first { it[NAME] in typeNames }
    }

internal fun JSONObject.parameter(name: String): JSONObject {
    return flatMap(PARAMETERS)
        .first { it[NAME] == name }
}

internal val JSONObject.firstParameter: JSONObject
    get() = get(PARAMETERS)
        .get(0) as JSONObject

internal val JSONObject.secondParameter: JSONObject
    get() = get(PARAMETERS)
        .get(1) as JSONObject

internal fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(RETURNS)) {
        sequenceOf(get(RETURNS))
    } else {
        emptySequence()
    }

internal fun JSONObject.addGeneric(generic: String) {
    val type = get(TYPE)
    set(TYPE, "$type<$generic>")
}

internal fun JSONObject.addExtendsGeneric(generic: String) {
    val type = get(EXTENDS)
    set(EXTENDS, "$type<$generic>")
}
