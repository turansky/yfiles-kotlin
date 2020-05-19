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

    get(IMPLEMENTS).apply {
        put(0, getString(0).replace("<$JS_ANY>", "<T>"))
    }

    (flatMap(CONSTRUCTORS) + flatMap(METHODS))
        .flatMap { it.optFlatMap(PARAMETERS) + it.returnsSequence() }
        .plus(flatMap(PROPERTIES))
        .forEach {
            val newType = when (val type = it[TYPE]) {
                JS_ANY, JS_OBJECT -> "T"
                ICURSOR -> "$ICURSOR<T>"

                else -> type
                    .replace("<$JS_ANY>", "<T>")
                    .replace("<$JS_OBJECT>", "<T>")
            }
            it[TYPE] = newType
        }
}

private fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(RETURNS)) {
        sequenceOf(get(RETURNS))
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
        ).optFlatMap(METHODS)
        .forEach {
            val methodName = it[NAME]

            it.optFlatMap(PARAMETERS)
                .filter { it[TYPE] == YLIST }
                .forEach {
                    val generic = getGeneric(methodName, it[NAME])
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

        methodName == "setNodeOrder" && parameterName == "list" ->
            return NODE

        methodName == "createSegmentInfoComparer" && parameterName == "channels" ->
            return "yfiles.router.Channel"
    }

    return when (parameterName) {
        "path", "points" -> YPOINT
        "nodeLabels" -> INODE_LABEL_LAYOUT
        "edgeLabels" -> IEDGE_LABEL_LAYOUT
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
    get(RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(TYPE) == YLIST)

    set(TYPE, ylist(generic))
}
