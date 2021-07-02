package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

internal const val LAYOUT_DESCRIPTOR = "yfiles.layout.LayoutDescriptor"

internal fun generateLayoutDescriptorUtils(context: GeneratorContext) {
    // language=kotlin
    context[LAYOUT_DESCRIPTOR] =
        """
            external interface LayoutDescriptor
        """.trimIndent()
}

internal fun applyLayoutDescriptorHacks(source: Source) {
    source.type("LayoutExecutorAsync").apply {
        flatMap(CONSTRUCTORS)
            .flatMap(PARAMETERS)
            .plus(flatMap(PROPERTIES))
            .filter { it[NAME] == "layoutDescriptor" }
            .forEach { it[TYPE] = LAYOUT_DESCRIPTOR }
    }

    source.functionSignature("yfiles.layout.WorkerHandler")
        .parameter("descriptor")
        .set(TYPE, LAYOUT_DESCRIPTOR)
}
