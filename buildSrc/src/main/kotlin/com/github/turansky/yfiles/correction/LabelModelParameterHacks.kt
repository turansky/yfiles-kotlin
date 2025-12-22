package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ILABEL_MODEL_PARAMETER

internal fun applyLabelModelParameterHacks(source: Source) {
    source.types(
        "ExtendedNodeLabelCandidate"
    ).forEach { it.property("parameter")[TYPE] = ILABEL_MODEL_PARAMETER }

    source.types(
        "ExtendedEdgeLabelCandidate",
        "ExtendedNodeLabelCandidate",
    ).flatMap { it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS) }
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "param" }
        .forEach { it[TYPE] = ILABEL_MODEL_PARAMETER }
}
