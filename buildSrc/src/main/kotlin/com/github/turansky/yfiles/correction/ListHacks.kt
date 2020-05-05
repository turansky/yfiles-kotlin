package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

private val DEFAULT_LISTS = setOf(
    list(JS_ANY),
    list(JS_OBJECT)
)

private fun list(generic: String): String =
    "yfiles.collections.IList<$generic>"

internal fun applyListHacks(source: Source) {
    fixProperty(source)
    fixMethodParameter(source)
    fixReturnType(source)
}

private fun fixProperty(source: Source) {
    sequenceOf(
        Triple("CompositeLayoutStage", "layoutStages", "yfiles.layout.ILayoutStage"),

        Triple("EdgeInfo", "edgeCellInfos", "yfiles.router.EdgeCellInfo"),
        Triple("EdgeRouter", "registeredPartitionExtensions", "yfiles.router.IGraphPartitionExtension"),
        Triple("EdgeRouter", "registeredPathSearchExtensions", "yfiles.router.PathSearchExtension"),

        Triple("EdgeRouterEdgeLayoutDescriptor", "intermediateRoutingPoints", YPOINT),
        Triple("SegmentGroup", "segmentInfos", SEGMENT_INFO),

        Triple("RotatableNodePlacerBase", "createdChildren", NODE)
    ).forEach { (className, propertyName, generic) ->
        source.type(className)
            .get(PROPERTIES)
            .get(propertyName)
            .fixTypeGeneric(generic)
    }
}

private fun fixMethodParameter(source: Source) {
    source.types(
        "YPointPath",

        "ChannelBasedPathRouting",
        "DynamicObstacleDecomposition",
        "GraphPartition",
        "GraphPartitionExtensionAdapter",

        "IDecompositionListener",
        "IEnterIntervalCalculator",
        "IObstaclePartition",

        "EdgeRouterPath",
        "PathSearchExtension",
        "SegmentGroup",
        "SegmentInfo",

        "RootNodeAlignment"
    ).flatMap { it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS) + it.optFlatMap(STATIC_METHODS) }
        .flatMap { it.optFlatMap(PARAMETERS) }
        .filter { it[TYPE] in DEFAULT_LISTS }
        .forEach {
            val generic = when (it[NAME]) {
                "l" -> YPOINT

                "subCells" -> PARTITION_CELL
                "obstacles" -> OBSTACLE
                "allEnterIntervals" -> "yfiles.router.OrthogonalInterval"
                "segmentInfos" -> SEGMENT_INFO
                "cellSegmentInfos" -> "yfiles.router.CellSegmentInfo"
                "entrances", "allStartEntrances" -> "yfiles.router.CellEntrance"

                "shapes" -> "yfiles.tree.RotatedSubtreeShape"

                else -> throw IllegalStateException("No generic found!")
            }

            it.fixTypeGeneric(generic)
        }
}

private fun fixReturnType(source: Source) {
    source.types(
        "IPartition",
        "IObstaclePartition",
        "DynamicObstacleDecomposition",
        "GraphPartition"
    ).flatMap(METHODS)
        .forEach {
            val generic = when (it[NAME]) {
                "getCells",
                "getCellsForNode",
                "getCellsForObstacle",
                "getNeighbors" -> PARTITION_CELL

                "getNodes" -> NODE
                "getObstacles" -> OBSTACLE

                else -> return@forEach
            }

            it.fixReturnTypeGeneric(generic)
        }

    source.type("YPointPath")
        .method("toList")
        .fixReturnTypeGeneric(YPOINT)
}

private fun JSONObject.fixReturnTypeGeneric(generic: String) {
    get(RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(TYPE) in DEFAULT_LISTS)

    set(TYPE, list(generic))
}
