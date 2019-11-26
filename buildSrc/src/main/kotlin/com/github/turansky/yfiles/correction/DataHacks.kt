package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_OBJECT

internal fun applyDataHacks(source: Source) {
    fixClass(source)
}

private val MAP_INTERFACES = setOf(
    "yfiles.algorithms.IEdgeMap",
    "yfiles.algorithms.INodeMap"
)

private fun fixClass(source: Source) {
    MAP_INTERFACES.forEach {
        source.type(it.substringAfterLast("."))
            .setSingleTypeParameter("V", JS_OBJECT)
    }
}
