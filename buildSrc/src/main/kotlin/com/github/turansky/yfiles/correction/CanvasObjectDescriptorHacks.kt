package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

internal fun applyCanvasObjectDescriptorHacks(source: Source) {
    source.type("ICanvasObjectDescriptor").apply {
        setSingleTypeParameter("in T")

        fixUserObjectType("T")

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

    source.type("DefaultPortCandidateDescriptor").apply {
        get(IMPLEMENTS).apply {
            put(indexOf(ICANVAS_OBJECT_DESCRIPTOR), "$ICANVAS_OBJECT_DESCRIPTOR<$TAG?>")
        }

        fixUserObjectType("$TAG?")
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

        flatMap(METHODS)
            .filter { it.has(PARAMETERS) }
            .filter { it.firstParameter[TYPE] == ICANVAS_OBJECT_DESCRIPTOR }
            .forEach {
                val typeParameter = between(it[RETURNS][TYPE], "<", ">")
                it.firstParameter.addGeneric(typeParameter)
            }
    }
}

private fun JSONObject.fixUserObjectType(type: String) {
    flatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "forUserObject" }
        .forEach { it[TYPE] = type }
}
