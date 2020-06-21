package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.get

private const val IPLANE_OBJECT = "yfiles.algorithms.IPlaneObject"

internal fun applyIntersectionHacks(source: Source) {
    source.type("IntersectionAlgorithm")[METHODS]["intersect"].apply {
        setSingleTypeParameter(bound = IPLANE_OBJECT)

        flatMap(PARAMETERS)
            .forEach { it.addGeneric("T") }
    }

    source.type("IIntersectionHandler") {
        setSingleTypeParameter(bound = IPLANE_OBJECT)

        method("checkIntersection")
            .flatMap(PARAMETERS)
            .forEach { it[TYPE] = "T" }

            method("create")
                .get(RETURNS)
                .addGeneric("T")
    }
}
