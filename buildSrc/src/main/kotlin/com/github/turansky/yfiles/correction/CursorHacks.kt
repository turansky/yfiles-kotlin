package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.removeAllObjects
import org.json.JSONObject

private fun cursor(generic: String): String =
    "$ICURSOR<$generic>"

internal fun applyCursorHacks(source: Source) {
    fixCursor(source)
    fixMethodParameter(source)
}

private fun fixCursor(source: Source) {
    source.type("ICursor")
        .fixGeneric()
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter("out T")

    property("current")[TYPE] = "T"
}

private fun fixMethodParameter(source: Source) {
    val nodeParameterNames = setOf(
        "subNodes",
        "nodeSubset"
    )

    source.types(
        "Graph",
        "LayoutGraph",
    ).flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] in nodeParameterNames }
        .forEach { it.fixTypeGeneric(NODE) }
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(TYPE) == ICURSOR)

    set(TYPE, cursor(generic))
}
