package com.github.turansky.yfiles.correction

internal fun applyEventHacks(source: Source) {
    source.types()
        .filter { it.has(J_METHODS) }
        .jsequence(J_METHODS)
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filter { it.getString(J_NAME) == "evt" }
        .forEach { it.put(J_NAME, "event") }
}