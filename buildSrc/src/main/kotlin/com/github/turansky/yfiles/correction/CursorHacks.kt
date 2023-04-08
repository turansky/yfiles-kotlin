package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.removeAllObjects
import org.json.JSONObject

private fun cursor(generic: String): String =
    "$ICURSOR<$generic>"

internal fun applyCursorHacks(source: Source) {
    fixCursor(source)
    fixCursorUtil(source)
    fixMethodParameter(source)
    fixReturnType(source)
}

private fun fixCursor(source: Source) {
    source.type("ICursor")
        .fixGeneric()

    sequenceOf(
        "IEdgeCursor" to EDGE,
        "ILineSegmentCursor" to "yfiles.algorithms.LineSegment",
        "INodeCursor" to NODE,
        "IPointCursor" to YPOINT
    ).forEach { (className, generic) ->
        source.type(className) {
            get(IMPLEMENTS).apply {
                put(0, getString(0) + "<$generic>")
            }

            val duplicatedPropertyName = className
                .removePrefix("I")
                .removeSuffix("Cursor")
                .replaceFirstChar { it.lowercase() }

            get(PROPERTIES).removeAllObjects {
                it[NAME] == duplicatedPropertyName
            }
        }
    }
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter("out T", YOBJECT)

    property("current")[TYPE] = "T"
}

private fun fixCursorUtil(source: Source) {
    source.type("Cursors").apply {
        flatMap(METHODS)
            .onEach {
                val bound = when (it[NAME]) {
                    "createNodeCursor" -> NODE
                    "createEdgeCursor" -> EDGE
                    else -> YOBJECT
                }

                it.setSingleTypeParameter(bound = bound)
            }
            .forEach {
                it.flatMap(PARAMETERS)
                    .plus(it[RETURNS])
                    .filter { it[TYPE] == ICURSOR }
                    .forEach { it[TYPE] = cursor("T") }
            }

        method("toArray").apply {
            sequenceOf(secondParameter, get(RETURNS))
                .forEach {
                    it.replaceInType("<$JS_ANY>", "<T>")
                    it.replaceInType("<$JS_OBJECT>", "<T>")
                }

            secondParameter[MODIFIERS]
                .put(OPTIONAL)
        }
    }
}

private fun fixMethodParameter(source: Source) {
    val nodeParameterNames = setOf(
        "subNodes",
        "nodeSubset"
    )

    source.types(
        "Graph",
        "LayoutGraph",
        "DefaultLayoutGraph"
    ).flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] in nodeParameterNames }
        .forEach { it.fixTypeGeneric(NODE) }

    source.types(
        "GraphPartitionManager",
        "LayoutGraphHider"
    ).map { it.method("hideItemCursor") }
        .map { it.firstParameter }
        .forEach { it.fixTypeGeneric(GRAPH_OBJECT) }
}

private fun fixReturnType(source: Source) {
    source.type("YPointPath")
        .method("cursor")
        .fixReturnTypeGeneric(YPOINT)

    source.type("PathAlgorithm")
        .method("findAllPathsCursor")
        .fixReturnTypeGeneric(EDGE_LIST)

    source.type("ShortestPathAlgorithm")
        .method("kShortestPathsCursor")
        .fixReturnTypeGeneric(EDGE_LIST)
}

private fun JSONObject.fixReturnTypeGeneric(generic: String) {
    get(RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(TYPE) == ICURSOR)

    set(TYPE, cursor(generic))
}
