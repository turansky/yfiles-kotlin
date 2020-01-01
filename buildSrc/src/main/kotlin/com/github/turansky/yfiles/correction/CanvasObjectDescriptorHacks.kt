package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ILOOKUP
import com.github.turansky.yfiles.IVISUAL_CREATOR
import com.github.turansky.yfiles.JS_VOID
import com.github.turansky.yfiles.VISUAL
import com.github.turansky.yfiles.json.get

internal fun applyCanvasObjectDescriptorHacks(source: Source) {
    source.type("ICanvasObjectDescriptor").apply {
        setSingleTypeParameter("in T")

        flatMap(METHODS)
            .filterNot { it[NAME] == "isDirty" }
            .map { it[PARAMETERS]["forUserObject"] }
            .forEach { it[TYPE] = "T" }

        sequenceOf(
            "ALWAYS_DIRTY_INSTANCE" to IVISUAL_CREATOR,
            "ALWAYS_DIRTY_LOOKUP" to ILOOKUP,
            "DYNAMIC_DIRTY_INSTANCE" to IVISUAL_CREATOR,
            "DYNAMIC_DIRTY_LOOKUP" to ILOOKUP,
            "VISUAL" to VISUAL,
            "VOID" to JS_VOID
        ).forEach { (name, typeParameter) ->
            get(CONSTANTS)[name].addGeneric(typeParameter)
        }
    }
}
