package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

private const val DRAG_DROP_DATA = "yfiles.view.DragDropData"

internal fun generateDragDropData(context: GeneratorContext) {
    // language=kotlin
    context[DRAG_DROP_DATA] =
        """
            |package yfiles.view
            |
            |external interface DragDropData
        """.trimMargin()
}

internal fun applyDragDropDataHacks(source: Source) {
    source.type("DragDropItem")
}
