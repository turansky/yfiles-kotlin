package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.EDGE
import com.github.turansky.yfiles.YOBJECT
import org.json.JSONObject

private const val LIST_CELL = "yfiles.algorithms.ListCell"

internal fun applyListCellHacks(source: Source) {
    source.type("ListCell") {
        setSingleTypeParameter(bound = YOBJECT)

        property("info")[TYPE] = "T"

        flatMap(METHODS)
            .forEach { it[RETURNS].addGeneric("T") }
    }

    source.type("YList")
        .getTypeHolders()
        .filter { it[TYPE] == LIST_CELL }
        .forEach { it.addGeneric("T") }

    source.type("BorderLine")
        .flatMap(METHODS)
        .filter { it[NAME] == "getValueAt" }
        .flatMap(PARAMETERS)
        .single { it[TYPE] == LIST_CELL }
        .addGeneric("BorderLineSegment")

    source.type("INodeData")
        .property("firstSameLayerEdgeCell")
        .addGeneric(EDGE)
}

private fun JSONObject.getTypeHolders(): Sequence<JSONObject> =
    flatMap(METHODS)
        .flatMap { it.optFlatMap(PARAMETERS) + it.returnsSequence() }
        .plus(flatMap(PROPERTIES))

private fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(RETURNS)) {
        sequenceOf(get(RETURNS))
    } else {
        emptySequence()
    }
