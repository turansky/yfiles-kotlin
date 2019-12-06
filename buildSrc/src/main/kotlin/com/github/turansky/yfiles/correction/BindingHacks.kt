package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import java.io.File

internal fun generateBindingUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/binding/Binding.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.binding
                |
                |external interface Binding
                |
                |fun Binding(source:Any):Binding = 
                |    source.unsafeCast<Binding>()
            """.trimMargin()
        )
}

internal fun applyBindingHacks(source: Source) {
    source.types(
        "GraphBuilder",
        "TreeBuilder",
        "AdjacentNodesGraphBuilder"
    ).flatMap(PROPERTIES)
        .filter { it[NAME].endsWith("Binding") }
        .filter { it[TYPE] == JS_ANY }
        .forEach { it[TYPE] = "Binding" }
}
