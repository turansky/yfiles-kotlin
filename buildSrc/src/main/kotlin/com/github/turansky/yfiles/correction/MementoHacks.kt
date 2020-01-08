package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

private const val LAYER_CONSTRAINTS_MEMENTO = "yfiles.hierarchic.LayerConstraintsMemento"
private const val SEQUENCE_CONSTRAINTS_MEMENTO = "yfiles.hierarchic.SequenceConstraintsMemento"

internal fun generateMementoUtils(context: GeneratorContext) {
    // language=kotlin
    context["yfiles.hierarchic.Mementos"] = """
            |package yfiles.hierarchic
            |
            |@JsName("Object")
            |external class LayerConstraintsMemento 
            |internal constructor()
            |
            |@JsName("Object")
            |external class SequenceConstraintsMemento 
            |internal constructor()
        """.trimMargin()
}

internal fun applyMementoHacks(source: Source) {
    source.type("ILayerConstraintFactory")
        .property("memento")[TYPE] = LAYER_CONSTRAINTS_MEMENTO

    source.type("ISequenceConstraintFactory")
        .property("memento")[TYPE] = SEQUENCE_CONSTRAINTS_MEMENTO
}
