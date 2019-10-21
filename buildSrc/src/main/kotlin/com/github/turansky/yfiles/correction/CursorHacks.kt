package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

private fun cursor(generic: String): String =
    "$CURSOR<$generic>"

internal fun applyCursorHacks(source: Source) {
    fixCursor(source)
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
            .getJSONArray(J_IMPLEMENTS).apply {
                put(0, getString(0) + "<$generic>")
            }
    }
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter("out T", ANY)

    property("current")
        .put(J_TYPE, "T")
}