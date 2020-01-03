package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ILABEL_MODEL_PARAMETER
import com.github.turansky.yfiles.json.get

internal fun applyLabelModelParameterHacks(source: Source) {
    source.types(
        "ILabelLayout",
        "LabelLayoutBase",

        "LabelCandidate"
    ).forEach { it[PROPERTIES]["modelParameter"][TYPE] = ILABEL_MODEL_PARAMETER }

    source.types(
        "LabelCandidate",

        "EdgeLabelCandidate",
        "ExtendedEdgeLabelCandidate",

        "NodeLabelCandidate",
        "ExtendedNodeLabelCandidate",

        "LayoutGraphUtilities"
    ).flatMap { it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS) + it.optFlatMap(STATIC_METHODS) }
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "param" }
        .forEach { it[TYPE] = ILABEL_MODEL_PARAMETER }

    source.types(
        "IEdgeLabelLayoutModel",
        "DiscreteEdgeLabelLayoutModel",
        "FreeEdgeLabelLayoutModel",
        "SliderEdgeLabelLayoutModel",

        "INodeLabelLayoutModel",
        "DiscreteNodeLabelLayoutModel",
        "FreeNodeLabelLayoutModel"
    ).onEach { it[PROPERTIES]["defaultParameter"][TYPE] = ILABEL_MODEL_PARAMETER }
        .onEach { it[METHODS]["createModelParameter"][RETURNS][TYPE] = ILABEL_MODEL_PARAMETER }
        .flatMap { it.flatMap(METHODS) + it.optFlatMap(STATIC_METHODS) }
        .flatMap(PARAMETERS)
        .filter { it[NAME] == "parameter" }
        .forEach { it[TYPE] = ILABEL_MODEL_PARAMETER }

    source.type("DiscreteEdgeLabelLayoutModel")
        .staticMethod("createPositionParameter")
        .get(RETURNS)[TYPE] = ILABEL_MODEL_PARAMETER
}
