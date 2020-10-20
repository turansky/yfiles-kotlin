package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.jArray
import com.github.turansky.yfiles.json.jObject

internal fun applyResultHacks(source: Source) {
    source.types()
        .filter { it[GROUP] == "class" }
        .filter { FINAL in it[MODIFIERS] }
        .filterNot { ENUM_LIKE in it[MODIFIERS] }
        .filterNot { it.has(EXTENDS) }
        .filterNot { it.has(CONSTRUCTORS) }
        .filter { it.has(PROPERTIES) }
        .filter { it.flatMap(PROPERTIES).all { it[MODIFIERS].let { RO in it && STATIC !in it } } }
        .filter { it.optFlatMap(METHODS).all { STATIC !in it[MODIFIERS] } }
        .forEach {
            it[CONSTRUCTORS] = jArray(
                jObject(
                    MODIFIERS to arrayOf(PRIVATE)
                )
            )
        }
}
