package com.github.turansky.yfiles.correction

import org.json.JSONObject

private const val LIST_CELL = "yfiles.algorithms.ListCell"

internal fun applyListCellHacks(source: Source) {
    source.type("ListCell") {
        setSingleTypeParameter()

        property("info")[TYPE] = "T"

        optFlatMap(METHODS)
            .forEach { it[RETURNS].addGeneric("T") }
    }

    source.type("YList")
        .getTypeHolders()
        .filter { it[TYPE] == LIST_CELL }
        .forEach { it.addGeneric("T") }
}

private fun JSONObject.getTypeHolders(): Sequence<JSONObject> =
    flatMap(METHODS)
        .flatMap { it.optFlatMap(PARAMETERS) + it.returnsSequence() }
        .plus(flatMap(PROPERTIES))
