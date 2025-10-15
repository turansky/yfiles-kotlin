package com.github.turansky.yfiles.correction

internal fun applyVisualHacks(source: Source) {
    source.types("NodeStyleBase", "EdgeStyleBase")
        .optFlatMap(TYPE_PARAMETERS)
        .filter { it[NAME] == "TVisual" }
        .forEach { it.remove(BOUNDS) }
}