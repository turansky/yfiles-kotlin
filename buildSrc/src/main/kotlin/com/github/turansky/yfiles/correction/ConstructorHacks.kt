package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.OPTIONAL
import com.github.turansky.yfiles.json.removeItem
import org.json.JSONObject

internal fun fixConstructors(source: Source) {
    source.types()
        .forEach(::mergeConstructors)

    fixOptionality(source)
}

private fun fixOptionality(source: Source) {
    source.types(
        "BendDecorator",
        "EdgeDecorator",
        "LabelDecorator",
        "NodeDecorator",
        "PortDecorator",
        "StripeLabelDecorator"
    ).forEach {
        it.flatMap(CONSTRUCTORS)
            .single()
            .firstParameter
            .changeOptionality(true)
    }

    source.types(
        "BendConverter",
        "MinimumNodeSizeStage",
        "MultiPageLayout",
        "OrganicPartitionGridLayoutStage",
        "TreeComponentLayout"
    ).flatMap(CONSTRUCTORS)
        .filter { it.has(PARAMETERS) }
        .filter { it[PARAMETERS].length() == 1 }
        .map { it.firstParameter }
        .filter { "core" in it[NAME] }
        .forEach { it.changeOptionality(true) }
}

private fun mergeConstructors(type: JSONObject) {
    if (!type.has(CONSTRUCTORS)) {
        return
    }

    val constructors = type[CONSTRUCTORS]
    if (constructors.length() != 2) {
        return
    }

    val (firstConstructor, secondConstructor) = getConstructorPair(
        constructors.getJSONObject(0),
        constructors.getJSONObject(1)
    ) ?: return

    if (!mergeRequired(firstConstructor, secondConstructor)) {
        return
    }

    constructors.removeItem(firstConstructor)
    secondConstructor
        .get(PARAMETERS)
        .let { it.getJSONObject(it.length() - 1) }
        .also { if (!it.has(MODIFIERS)) it[MODIFIERS] = emptyList<String>() }
        .get(MODIFIERS)
        .put(OPTIONAL)
}

private fun getConstructorPair(
    first: JSONObject,
    second: JSONObject
): Pair<JSONObject, JSONObject>? =
    when (first.parametersCount - second.parametersCount) {
        -1 -> first to second
        1 -> second to first
        else -> null
    }

private fun mergeRequired(
    first: JSONObject,
    second: JSONObject
): Boolean {
    if (first.parametersCount == 0) {
        return true
    }

    val firstNames = first.parametersNames
    val secondNames = second.parametersNames
    return firstNames
        .asSequence()
        .mapIndexed { index, item -> item == secondNames[index] }
        .all { it }
}

private val JSONObject.parametersCount: Int
    get() = when {
        has(PARAMETERS) -> get(PARAMETERS).length()
        else -> 0
    }

private val JSONObject.parametersNames: List<String>
    get() = if (has(PARAMETERS)) {
        flatMap(PARAMETERS)
            .map { it[NAME] }
            .toList()
    } else {
        emptyList()
    }
