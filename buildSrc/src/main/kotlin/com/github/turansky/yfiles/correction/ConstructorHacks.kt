package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.OPTIONAL
import com.github.turansky.yfiles.json.removeItem
import org.json.JSONObject

internal fun mergeConstructors(source: Source) {
    source.types()
        .forEach(::mergeConstructors)
}

private fun mergeConstructors(type: JSONObject) {
    if (!type.has(J_CONSTRUCTORS)) {
        return
    }

    val constructors = type.getJSONArray(J_CONSTRUCTORS)
    if (constructors.length() != 2) {
        return
    }

    val pair = getConstructorPair(
        constructors.getJSONObject(0),
        constructors.getJSONObject(1)
    ) ?: return

    constructors.removeItem(pair.first)
    pair.second.firstParameter
        .getJSONArray(J_MODIFIERS)
        .put(OPTIONAL)
}

private fun getConstructorPair(
    first: JSONObject,
    second: JSONObject
): Pair<JSONObject, JSONObject>? =
    when {
        first.parametersCount == 0 && second.parametersCount == 1 -> first to second
        second.parametersCount == 0 && first.parametersCount == 1 -> second to first
        else -> null
    }

private val JSONObject.parametersCount: Int
    get() = when {
        has(J_PARAMETERS) -> getJSONArray(J_PARAMETERS).length()
        else -> 0
    }