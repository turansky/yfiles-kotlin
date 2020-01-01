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

    source.type("ICanvasObject")[PROPERTIES]["descriptor"].addGeneric("*")
    source.type("ICanvasObjectGroup")[METHODS]["addChild"].secondParameter.addGeneric("*")

    source.type("DefaultPortCandidateDescriptor").apply {
        get(IMPLEMENTS).apply {
            put(indexOf(ICANVAS_OBJECT_DESCRIPTOR), "$ICANVAS_OBJECT_DESCRIPTOR<$TAG?>")
        }

        fixUserObjectType("$TAG?")
    }

    source.type("CreateEdgeInputMode")
        .flatMap(PROPERTIES)
        .filter { it[TYPE] == ICANVAS_OBJECT_DESCRIPTOR }
        .forEach { it.addGeneric("$TAG?") }

    source.types("PortRelocationHandle", "SnapContext")
        .flatMap(METHODS)
        .filter { it.has(RETURNS) }
        .map { it[RETURNS] }
        .filter { it[TYPE] == ICANVAS_OBJECT_DESCRIPTOR }
        .forEach { it.addGeneric("$TAG?") }

    // TODO: check required
    source.type("LabelPositionHandler")
        .flatMap(METHODS)
        .filter { it.has(RETURNS) }
        .map { it[RETURNS] }
        .filter { it[TYPE] == ICANVAS_OBJECT_DESCRIPTOR }
        .forEach { it.addGeneric("*") }

    source.type("ItemModelManager").apply {
        get(PROPERTIES)["descriptor"].addGeneric("T")
        get(METHODS)["getDescriptor"][RETURNS].addGeneric("T")
    }

    source.type("GraphModelManager").apply {
        flatMap(PROPERTIES)
            .filter { it[TYPE] == ICANVAS_OBJECT_DESCRIPTOR }
            .forEach {
                val typeParameter = when (val name = it[NAME]) {
                    "nodeDescriptor" -> INODE
                    "edgeDescriptor" -> IEDGE
                    "portDescriptor" -> IPORT
                    else -> {
                        require(name.endsWith("LabelDescriptor"))
                        ILABEL
                    }
                }

                it.addGeneric(typeParameter)
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
