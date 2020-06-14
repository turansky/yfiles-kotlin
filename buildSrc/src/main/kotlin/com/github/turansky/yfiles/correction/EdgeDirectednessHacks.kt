package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_NUMBER

internal const val EDGE_DIRECTEDNESS = "yfiles.algorithms.EdgeDirectedness"

internal fun generateEdgeDirectednessUtils(context: GeneratorContext) {
    context[EDGE_DIRECTEDNESS] = """
        typealias EdgeDirectedness = Double
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
