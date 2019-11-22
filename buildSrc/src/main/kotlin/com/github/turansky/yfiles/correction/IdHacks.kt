package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.YID
import java.io.File

internal fun generateIdUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/lang/Id.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.lang
                |
                |external interface Id
                |
                |fun Id(source:Any):Id = 
                |    source.unsafeCast<Id>()
            """.trimMargin()
        )
}

private val ID_DP_KEYS = setOf(
    "yfiles.algorithms.EdgeDpKey<$JS_ANY>",
    "yfiles.algorithms.NodeDpKey<$JS_ANY>",

    "yfiles.algorithms.IEdgeLabelLayoutDpKey<$JS_ANY>",
    "yfiles.algorithms.INodeLabelLayoutDpKey<$JS_ANY>"
)

internal fun applyIdHacks(source: Source) {
    source.types()
        .flatMap { it.optJsequence(J_CONSTANTS) }
        .filter { it.getString(J_TYPE) in ID_DP_KEYS }
        .filter { it.getString(J_NAME).endsWith("_ID_DP_KEY") }
        .forEach {
            val newType = it.getString(J_TYPE)
                .replace("<$JS_ANY>", "<$YID>")

            it.put(J_TYPE, newType)
        }

    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    source.types()
        .flatMap {
            it.optJsequence(J_METHODS)
                .filter { it.has(J_PARAMETERS) }
                .jsequence(J_PARAMETERS)
                .plus(it.optJsequence(J_PROPERTIES))
        }
        .filter { it.getString(J_TYPE) in likeObjectTypes }
        .filter { it.getString(J_NAME).endsWith("Id") }
        .forEach { it.put(J_TYPE, YID) }
}
