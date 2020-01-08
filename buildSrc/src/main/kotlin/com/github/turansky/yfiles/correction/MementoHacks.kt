package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.json.get

internal const val LAYER_CONSTRAINTS_MEMENTO = "yfiles.hierarchic.LayerConstraintsMemento"
internal const val SEQUENCE_CONSTRAINTS_MEMENTO = "yfiles.hierarchic.SequenceConstraintsMemento"

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

    source.type("HierarchicLayout")
        .get(CONSTANTS).apply {
            sequenceOf(
                "LAYER_CONSTRAINTS_MEMENTO_DP_KEY" to LAYER_CONSTRAINTS_MEMENTO,
                "SEQUENCE_CONSTRAINTS_MEMENTO_DP_KEY" to SEQUENCE_CONSTRAINTS_MEMENTO
            ).forEach { (name, typeParameter) ->
                get(name).also {
                    require(it[TYPE] == graphDpKey(JS_ANY))
                    it[TYPE] = graphDpKey(typeParameter)
                }
            }
        }
}
