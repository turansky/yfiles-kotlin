package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
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

    source.type("GraphModelManager").apply {
        flatMap(PROPERTIES)
            .filter { it[TYPE] == ICANVAS_OBJECT_DESCRIPTOR }
            .forEach {
                it[TYPE] = when (val name = it[NAME]) {
                    "nodeDescriptor" -> INODE
                    "edgeDescriptor" -> IEDGE
                    "portDescriptor" -> IPORT
                    else -> {
                        require(name.endsWith("LabelDescriptor"))
                        ILABEL
                    }
                }
            }

        flatMap(CONSTANTS)
            .filter { it[TYPE] == ICANVAS_OBJECT_DESCRIPTOR }
            .forEach {
                val typeParameter = when (it[NAME]) {
                    "DEFAULT_EDGE_DESCRIPTOR" -> IEDGE
                    "DEFAULT_LABEL_DESCRIPTOR" -> ILABEL
                    "DEFAULT_NODE_DESCRIPTOR" -> INODE
                    "DEFAULT_PORT_DESCRIPTOR" -> IPORT
                    else -> TODO()
                }

                it.addGeneric(typeParameter)
            }
    }
}
