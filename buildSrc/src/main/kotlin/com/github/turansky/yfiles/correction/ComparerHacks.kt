package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.json.firstWithName
import org.json.JSONObject

private val NODE = "yfiles.algorithms.Node"
private val EDGE = "yfiles.algorithms.Edge"

internal fun applyComparerHacks(source: Source) {
    fixComparerInheritors(source)
    fixNodePlacers(source)
}

private fun fixComparerInheritors(source: Source) {
    sequenceOf(
        "DefaultOutEdgeComparer" to EDGE,
        "NodeOrderComparer" to NODE,
        "NodeWeightComparer" to NODE
    ).forEach { (className, generic) ->
        source.type(className)
            .apply {
                getJSONArray(J_IMPLEMENTS).apply {
                    require(length() == 1)
                    require(get(0) == "yfiles.collections.IComparer<$JS_ANY>")

                    put(0, "yfiles.collections.IComparer<$generic>")
                }

                getJSONArray(J_METHODS)
                    .firstWithName("compare")
                    .jsequence(J_PARAMETERS)
                    .forEach { it.put(J_TYPE, generic) }
            }
    }
}

private fun fixNodePlacers(source: Source) {
    source.types(
        "IFromSketchNodePlacer",
        "AspectRatioNodePlacer",
        "DefaultNodePlacer",
        "DendrogramNodePlacer",
        "GridNodePlacer",
        "RotatableNodePlacerBase"
    ).forEach {
        it.getJSONArray(J_METHODS)
            .firstWithName("createFromSketchComparer")
            .fixReturnTypeGeneric(NODE)
    }

    source.types(
        "RotatableNodePlacerBase",
        "AssistantNodePlacer",
        "BusNodePlacer",
        "LeftRightNodePlacer"
    ).forEach {
        it.getJSONArray(J_METHODS)
            .firstWithName("createComparer")
            .fixReturnTypeGeneric(EDGE)
    }
}

private fun JSONObject.fixReturnTypeGeneric(generic: String) {
    getJSONObject(J_RETURNS).apply {
        require(getString(J_TYPE) == "yfiles.collections.IComparer<$JS_ANY>")
        put(J_TYPE, "yfiles.collections.IComparer<$generic>")
    }
}