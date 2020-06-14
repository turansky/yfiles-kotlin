package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

internal const val EDGE_DIRECTEDNESS = "yfiles.algorithms.EdgeDirectedness"

internal fun generateEdgeDirectednessUtils(context: GeneratorContext) {
    context[EDGE_DIRECTEDNESS] = """
        typealias EdgeDirectedness = Double
    """.trimIndent()
}
