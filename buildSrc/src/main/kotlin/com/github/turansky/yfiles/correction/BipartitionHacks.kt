package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.get
import java.io.File

internal val BIPARTITION_MARK = "yfiles.algorithms.BipartitionMark"

internal fun generateBipartitionMark(sourceDir: File) {
    sourceDir.resolve("yfiles/algorithms/BipartitionMark.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.algorithms
                |
                |external interface BipartitionMark
            """.trimMargin()
        )
}

internal fun applyBipartitionHacks(source: Source) {
    val constants = source.type("BipartitionAlgorithm")[CONSTANTS]
    sequenceOf("RED", "BLUE")
        .forEach { constants[it][TYPE] = BIPARTITION_MARK }
}
