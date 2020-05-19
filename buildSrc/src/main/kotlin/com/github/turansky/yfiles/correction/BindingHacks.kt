package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY

private val BINDING = "yfiles.binding.Binding"

internal fun generateBindingUtils(context: GeneratorContext) {
    // language=kotlin
    context[BINDING] =
        """
            |external interface Binding
            |
            |fun Binding(source:Any):Binding = 
            |    source.unsafeCast<$BINDING>()
        """.trimMargin()
}

internal fun applyBindingHacks(source: Source) {
    source.types(
            "GraphBuilder",
            "TreeBuilder",
            "AdjacentNodesGraphBuilder"
        ).flatMap(PROPERTIES)
        .filter { it[NAME].endsWith("Binding") }
        .filter { it[TYPE] == JS_ANY }
        .forEach { it[TYPE] = BINDING }
}
