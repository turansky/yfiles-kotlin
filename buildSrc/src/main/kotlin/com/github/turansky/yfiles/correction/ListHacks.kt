package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

private val DEFAULT_LISTS = setOf(
    list(JS_ANY),
    list(JS_OBJECT)
)

private fun list(generic: String): String =
    "yfiles.collections.IList<$generic>"

internal fun applyListHacks(source: Source) {
    fixMethodParameter(source)
}

private fun fixMethodParameter(source: Source) {
    source.types(
        "PathSearchExtension",
    ).flatMap { it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS) }
        .flatMap { it.optFlatMap(PARAMETERS) }
        .filter { it[TYPE] in DEFAULT_LISTS }
        .forEach {
            val generic = when (it[NAME]) {
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

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(TYPE) in DEFAULT_LISTS)

    set(TYPE, list(generic))
}
