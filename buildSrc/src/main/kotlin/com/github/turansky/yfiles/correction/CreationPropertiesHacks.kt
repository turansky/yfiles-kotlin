package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.FINAL

internal fun applyCreationPropertiesHacks(source: Source) {
    source.type("CreationProperties")
        .flatMap(METHODS)
        .filter { !it[MODIFIERS].contains(FINAL) }
        .forEach { it[MODIFIERS].put(FINAL) }
}