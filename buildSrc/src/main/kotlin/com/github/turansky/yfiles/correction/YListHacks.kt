package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

private fun ylist(generic: String): String =
    "$YLIST<$generic>"

internal fun applyYListHacks(source: Source) {
    fixYList(source)
    fixMethodParameter(source)
    fixProperty(source)
    fixReturnType(source)
}

private fun fixYList(source: Source) {
    source.type("YList")
        .fixGeneric()

    source.type("YNodeList")
        .addExtendsGeneric(NODE)

    source.type("EdgeList")
        .addExtendsGeneric(EDGE)
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter(bound = JS_ANY)

    get(J_IMPLEMENTS).apply {
        put(0, getString(0).replace("<$JS_ANY>", "<T>"))
    }

    (jsequence(J_CONSTRUCTORS) + jsequence(J_METHODS))
        .flatMap { it.optJsequence(J_PARAMETERS) + it.returnsSequence() }
        .plus(jsequence(J_PROPERTIES))
        .forEach {
            val type = it[J_TYPE]
            val newType = when (type) {
                JS_ANY, JS_OBJECT -> "T"
                CURSOR -> "$CURSOR<T>"

                else -> type
                    .replace("<$JS_ANY>", "<T>")
                    .replace("<$JS_OBJECT>", "<T>")
            }
            it[J_TYPE] = newType
        }
}

private fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(J_RETURNS)) {
        sequenceOf(get(J_RETURNS))
    } else {
        emptySequence()
    }

private fun fixMethodParameter(source: Source) {
    source.types(
        "YList",

        "Geom",
        "TriangulationAlgorithm",
        "LayoutGraph",

        "LabelingBase",
        "SelfLoopCalculator",
        "IntersectionAlgorithm",
        "ChannelBasedPathRouting",
        "OrthogonalPatternEdgeRouter",

        "ILayer",

        "IElementFactory",
        "DefaultElementFactory",
        "MultiPageLayout"
    ).flatMap { it.optJsequence(J_METHODS) + it.optJsequence(J_STATIC_METHODS) }
        .forEach {
            val methodName = it[J_NAME]

            it.optJsequence(J_PARAMETERS)
                .filter { it[J_TYPE] == YLIST }
                .forEach {
                    val generic = getGeneric(methodName, it[J_NAME])
                    it.fixTypeGeneric(generic)
                }
        }
}

private fun getGeneric(
    methodName: String,
    parameterName: String
): String {
    when {
        methodName == "splice" && parameterName == "list" ->
            return "T"

        methodName == "intersect" && parameterName == "objects" ->
            return "yfiles.algorithms.IPlaneObject"

        methodName == "setNodeOrder" && parameterName == "list" ->
            return NODE

        methodName == "createSegmentInfoComparer" && parameterName == "channels" ->
            return "yfiles.router.Channel"
    }

    return when (parameterName) {
        "path", "points" -> YPOINT
        "nodeLabels" -> "yfiles.layout.INodeLabelLayout"
        "edgeLabels" -> "yfiles.layout.IEdgeLabelLayout"
        "selfLoops" -> EDGE

        "edgeIds", "originalEdgeIds" -> YID

        else -> throw IllegalStateException("No generic found!")
    }
}

private fun fixProperty(source: Source) {
    sequenceOf(
        Triple("ILayer", "sameLayerEdges", EDGE),
        Triple("EdgeCellInfo", "cellSegmentInfos", "yfiles.router.CellSegmentInfo")
    ).forEach { (className, propertyName, generic) ->
        source.type(className)
            .property(propertyName)
            .fixTypeGeneric(generic)
    }
}

private fun fixReturnType(source: Source) {
    sequenceOf(
        "INodeLabelLayoutModel" to NODE_LABEL_CANDIDATE,
        "DiscreteNodeLabelLayoutModel" to NODE_LABEL_CANDIDATE,
        "FreeNodeLabelLayoutModel" to NODE_LABEL_CANDIDATE,

        "IEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE,
        "DiscreteEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE,
        "FreeEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE,
        "SliderEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE
    ).forEach { (className, generic) ->
        source.type(className)
            .method("getLabelCandidates")
            .fixReturnTypeGeneric(generic)
    }

    sequenceOf(
        "LayoutGraph" to "getPathList",
        "LayoutGraph" to "getPointList",

        "EdgeInfo" to "calculatePathPoints"
    ).forEach { (className, methodName) ->
        source.type(className)
            .method(methodName)
            .fixReturnTypeGeneric(YPOINT)
    }

    sequenceOf(
        Triple("Geom", "calcConvexHull", YPOINT),
        Triple("ShortestPathAlgorithm", "kShortestPaths", EDGE_LIST)
    ).forEach { (className, methodName, generic) ->
        source.type(className)
            .staticMethod(methodName)
            .fixReturnTypeGeneric(generic)
    }
}

private fun JSONObject.fixReturnTypeGeneric(generic: String) {
    get(J_RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(J_TYPE) == YLIST)

    set(J_TYPE, ylist(generic))
}
