package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import org.json.JSONObject

private val DEFAULT_LISTS = setOf(
    list(JS_ANY),
    list(JS_OBJECT)
)

private fun list(generic: String): String =
    "yfiles.collections.IList<$generic>"

internal fun applyListHacks(source: Source) {
    fixProperty(source)
    fixReturnType(source)
}

private fun fixProperty(source: Source) {
    sequenceOf(
        Triple("CompositeLayoutStage", "layoutStages", "yfiles.layout.ILayoutStage"),

        Triple("EdgeInfo", "edgeCellInfos", "yfiles.router.EdgeCellInfo"),
        Triple("EdgeRouter", "registeredPartitionExtensions", "yfiles.router.IGraphPartitionExtension"),
        Triple("EdgeRouter", "registeredPathSearchExtensions", "yfiles.router.PathSearchExtension"),

        Triple("EdgeRouterEdgeLayoutDescriptor", "intermediateRoutingPoints", YPOINT),
        Triple("SegmentGroup", "segmentInfos", "yfiles.router.SegmentInfo"),

        Triple("RotatableNodePlacerBase", "createdChildren", YPOINT)
    ).forEach { (className, propertyName, generic) ->
        source.type(className)
            .getJSONArray(J_PROPERTIES)
            .firstWithName(propertyName)
            .fixTypeGeneric(generic)
    }
}

private fun fixReturnType(source: Source) {
    source.types(
        "IPartition",
        "IObstaclePartition",
        "DynamicObstacleDecomposition",
        "GraphPartition"
    ).jsequence(J_METHODS)
        .forEach {
            val generic = when (it.getString(J_NAME)) {
                "getCells",
                "getCellsForNode",
                "getCellsForObstacle",
                "getNeighbors" -> PARTITION_CELL

                "getNodes" -> NODE
                "getObstacles" -> "yfiles.router.Obstacle"

                else -> return@forEach
            }

            it.fixReturnTypeGeneric(generic)
        }
}

private fun Source.method(className: String, methodName: String) =
    type(className).method(methodName)

private fun JSONObject.fixReturnTypeGeneric(generic: String) {
    getJSONObject(J_RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(getString(J_TYPE) in DEFAULT_LISTS)

    put(J_TYPE, list(generic))
}