package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY

private val BINDING_LIKE = "yfiles.binding.BindingLike"

internal fun generateBindingUtils(context: GeneratorContext) {
    // language=kotlin
    context[BINDING_LIKE] =
        """
            |external interface BindingLike
            |
            |fun BindingLike(source:Any):BindingLike = 
            |    source.unsafeCast<$BINDING_LIKE>()
        """.trimMargin()
}

internal fun applyBindingHacks(source: Source) {
    source.types(
            "GraphBuilder",
            "TreeBuilder"
        ).flatMap(PROPERTIES)
        .filter { it[NAME].endsWith("Binding") }
        .filter { it[TYPE] == JS_ANY }
        .forEach { it[TYPE] = BINDING_LIKE }
}
