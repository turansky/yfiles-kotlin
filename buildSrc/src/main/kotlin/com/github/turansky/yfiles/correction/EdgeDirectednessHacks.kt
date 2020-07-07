package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.INLINE
import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_NUMBER

internal const val EDGE_DIRECTEDNESS = "yfiles.algorithms.EdgeDirectedness"

internal fun generateEdgeDirectednessUtils(context: GeneratorContext) {
    context[EDGE_DIRECTEDNESS, INLINE] = """
        @JsName("Number")
        sealed class EdgeDirectedness
        
        inline fun EdgeDirectedness(value: Double): $EDGE_DIRECTEDNESS = 
            value.unsafeCast<$EDGE_DIRECTEDNESS>()
        
        object EdgeDirectednesses {
            val SOURCE_TO_TARGET: $EDGE_DIRECTEDNESS = EdgeDirectedness(1.0)
            val TARGET_TO_SOURCE: $EDGE_DIRECTEDNESS = EdgeDirectedness(-1.0)
            val UNDIRECTED: $EDGE_DIRECTEDNESS = EdgeDirectedness(0.0)
        }
    """.trimIndent()
}

internal fun applyEdgeDirectednessHacks(source: Source) {
    source.types()
        .optFlatMap(PROPERTIES)
        .filter { it[NAME] == "edgeDirectedness" }
        .forEach { it.replaceInType(",$JS_NUMBER>", ",$EDGE_DIRECTEDNESS>") }

    source.types()
        .optFlatMap(CONSTANTS)
        .filter { it[NAME] == "EDGE_DIRECTEDNESS_DP_KEY" }
        .forEach { it.replaceInType("<$JS_NUMBER>", "<$EDGE_DIRECTEDNESS>") }
}
