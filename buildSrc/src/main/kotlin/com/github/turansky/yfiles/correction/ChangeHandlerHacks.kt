package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY

internal fun fixChangeHandlers(source: Source) {
    listOf(
            "yfiles.graph.BendLocationChangedHandler",
            "yfiles.graph.NodeLayoutChangedHandler",
    ).map(source::functionSignature)
        .forEach { sig ->
            sig.flatMap(PARAMETERS)
            .filter { it[NAME] == "source" }
            .forEach {
                it[TYPE] = JS_ANY
            }
        }
}