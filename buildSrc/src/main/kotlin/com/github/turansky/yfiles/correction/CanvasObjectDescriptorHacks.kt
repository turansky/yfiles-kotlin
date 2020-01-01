package com.github.turansky.yfiles.correction

internal fun applyCanvasObjectDescriptorHacks(source: Source) {
    source.type("ICanvasObjectDescriptor").apply {
        setSingleTypeParameter("in T")

        flatMap(METHODS)
            .flatMap(PARAMETERS)
            .filter { it[NAME] == "forUserObject" }
            .forEach { it[TYPE] = "T" }
    }
}
