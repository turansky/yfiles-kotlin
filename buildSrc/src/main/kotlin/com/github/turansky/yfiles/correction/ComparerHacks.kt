package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.json.firstWithName
import org.json.JSONObject

private val NODE = "yfiles.algorithms.Node"
private val EDGE = "yfiles.algorithms.Edge"
private val GRAPH_OBJECT = "yfiles.algorithms.GraphObject"

private val DEFAULT_COMPARERS = setOf(
    comparer(JS_ANY),
    comparer(JS_OBJECT)
)

private fun comparer(generic: String): String =
    "yfiles.collections.IComparer<$generic>"

internal fun applyComparerHacks(source: Source) {
    fixComparerInheritors(source)
    fixComparerUtilMethods(source)
    fixComparerAsMethodParameter(source)
    fixComparerAsProperty(source)
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
                    require(get(0) in DEFAULT_COMPARERS)

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

private fun fixComparerAsMethodParameter(source: Source) {
    source.types(
        "Graph",
        "YNode",

        "PortCandidateOptimizer",
        "PortConstraintOptimizerBase",

        "AssistantNodePlacer",
        "ChannelBasedPathRouting",
        "GivenSequenceSequencer"
    ).flatMap { it.jsequence(J_METHODS) + it.optJsequence(J_STATIC_METHODS) + it.optJsequence(J_CONSTRUCTORS) }
        .forEach {
            val methodName = it.getString(J_NAME)

            it.optJsequence(J_PARAMETERS)
                .filter { it.getString(J_TYPE) in DEFAULT_COMPARERS }
                .forEach {
                    val generic = getGeneric(methodName, it.getString(J_NAME))
                    it.fixTypeGeneric("in $generic")
                }
        }
}

private fun getGeneric(
    methodName: String,
    parameterName: String
): String {
    when (methodName) {
        "sortNodes",
        "GivenSequenceSequencer" -> return NODE

        "sortEdges", "sortInEdges", "sortOutEdges",
        "createCompoundComparer" -> return EDGE
    }

    return when (parameterName) {
        "inEdgeOrder", "outEdgeOrder" -> EDGE
        "segmentInfoComparer" -> "yfiles.router.SegmentInfo"
        else -> throw IllegalStateException("No generic found!")
    }
}

private fun fixComparerAsProperty(source: Source) {
    sequenceOf(
        Triple("TabularLayout", "nodeComparer", NODE),
        Triple("TreeMapLayout", "nodeComparer", NODE),
        Triple("GivenSequenceSequencer", "sequenceComparer", NODE),

        Triple("EdgeRouter", "edgeComparer", EDGE),
        Triple("SeriesParallelLayout", "defaultOutEdgeComparer", EDGE),
        Triple("TreeLayout", "defaultOutEdgeComparer", EDGE),

        Triple("AspectRatioTreeLayout", "comparer", EDGE),
        Triple("BalloonLayout", "comparer", EDGE),
        Triple("ClassicTreeLayout", "comparer", EDGE)
    ).forEach { (className, propertyName, generic) ->
        source.type(className)
            .getJSONArray(J_PROPERTIES)
            .firstWithName(propertyName)
            .fixTypeGeneric("in $generic")
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
    getJSONObject(J_RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(getString(J_TYPE) in DEFAULT_COMPARERS)

    put(J_TYPE, comparer(generic))
}