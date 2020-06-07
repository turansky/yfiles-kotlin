package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

private const val DRAG_DROP_DATA = "yfiles.view.DragDropData"

internal fun generateDragDropData(context: GeneratorContext) {
    // language=kotlin
    context[DRAG_DROP_DATA] = "external interface DragDropData"
}

internal fun applyDragDropDataHacks(source: Source) {
    source.type("DragDropItem") {
        (flatMap(CONSTRUCTORS) + flatMap(METHODS))
            .flatMap(PARAMETERS)
            .filter { it[NAME] == "data" }
            .plus(method("getData")[RETURNS])
            .forEach { it[TYPE] = DRAG_DROP_DATA }
    }

    source.type("DropInputMode")
        .property("dropData")[TYPE] = DRAG_DROP_DATA

    source.functionSignature("yfiles.input.DropCreationCallback")
        .parameter("dropData")[TYPE] = DRAG_DROP_DATA

    source.type("DragSource")
        .property("item")[TYPE] = "DragDropItem"
}
