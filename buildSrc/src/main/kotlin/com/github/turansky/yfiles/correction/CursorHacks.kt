package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

private fun cursor(generic: String): String =
    "$CURSOR<$generic>"

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
        source.type(className)
            .get(IMPLEMENTS).apply {
                put(0, getString(0) + "<$generic>")
            }
    }
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter("out T", JS_ANY)

    property("current")[J_TYPE] = "T"
}

private fun fixCursorUtil(source: Source) {
    source.type("Cursors").apply {
        jsequence(STATIC_METHODS)
            .onEach {
                val name = it[J_NAME]
                val bound = when (name) {
                    "createNodeCursor" -> NODE
                    "createEdgeCursor" -> EDGE
                    else -> JS_ANY
                }

                it.setSingleTypeParameter(bound = bound)
            }
            .forEach {
                it.jsequence(PARAMETERS)
                    .plus(it[RETURNS])
                    .filter { it[J_TYPE] == CURSOR }
                    .forEach { it[J_TYPE] = cursor("T") }
            }

        staticMethod("toArray").apply {
            sequenceOf(secondParameter, get(RETURNS))
                .forEach {
                    it[J_TYPE] = it[J_TYPE]
                        .replace("<$JS_ANY>", "<T>")
                        .replace("<$JS_OBJECT>", "<T>")
                }

            secondParameter[J_MODIFIERS]
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
    ).jsequence(CONSTRUCTORS)
        .flatMap { it.optJsequence(PARAMETERS) }
        .filter { it[J_NAME] in nodeParameterNames }
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
        .staticMethod("findAllPathsCursor")
        .fixReturnTypeGeneric(EDGE_LIST)

    source.type("ShortestPathAlgorithm")
        .staticMethod("kShortestPathsCursor")
        .fixReturnTypeGeneric(EDGE_LIST)
}

private fun JSONObject.fixReturnTypeGeneric(generic: String) {
    get(RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(J_TYPE) == CURSOR)

    set(J_TYPE, cursor(generic))
}
