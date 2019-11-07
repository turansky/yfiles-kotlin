package com.github.turansky.yfiles.correction

internal fun applyEventHacks(source: Source) {
    source.types()
        .flatMap { it.optJsequence(J_CONSTRUCTORS) + it.optJsequence(J_METHODS) }
        .plus(source.functionSignatures.run { keySet().map { getJSONObject(it) } })
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filter { it.getString(J_NAME) == "evt" }
        .forEach { it.put(J_NAME, "event") }
}