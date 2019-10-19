package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.json.firstWithName
import org.json.JSONObject

private val NODE = "yfiles.algorithms.Node"
private val EDGE = "yfiles.algorithms.Edge"
private val GRAPH_OBJECT = "yfiles.algorithms.GraphObject"

private val DEFAULT_COMPARER = comparer(JS_ANY)

private fun comparer(generic: String): String =
    "yfiles.collections.IComparer<$generic>"

internal fun applyComparerHacks(source: Source) {
    fixComparerInheritors(source)
    fixComparerUtilMethods(source)
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
                    require(get(0) == DEFAULT_COMPARER)

                    put(0, comparer(generic))
                }

                getJSONArray(J_METHODS)
                    .firstWithName("compare")
                    .jsequence(J_PARAMETERS)
                    .forEach { it.put(J_TYPE, generic) }
            }
    }
}

private fun fixComparerUtilMethods(source: Source) {
    val staticMethods = source.type("Comparers")
        .getJSONArray(J_STATIC_METHODS)

    sequenceOf(
        "createIntDataComparer" to GRAPH_OBJECT,
        "createNumberDataComparer" to GRAPH_OBJECT,

        "createIntDataSourceComparer" to EDGE,
        "createIntDataTargetComparer" to EDGE,
        "createNumberDataSourceComparer" to EDGE,
        "createNumberDataTargetComparer" to EDGE
    ).forEach { (methodName, generic) ->
        staticMethods.firstWithName(methodName)
            .fixReturnTypeGeneric(generic)
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
        require(getString(J_TYPE) == DEFAULT_COMPARER)
        put(J_TYPE, comparer(generic))
    }
}