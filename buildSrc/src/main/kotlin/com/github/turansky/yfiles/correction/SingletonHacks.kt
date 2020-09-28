package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.FINAL
import com.github.turansky.yfiles.PRIVATE
import com.github.turansky.yfiles.PROTECTED
import com.github.turansky.yfiles.json.jArray
import com.github.turansky.yfiles.json.jObject

internal fun applySingletonHacks(source: Source) {
    source.types()
        .filter { it[GROUP] == "class" }
        .filterNot { it.has(CONSTRUCTORS) }
        .filter { it.optFlatMap(CONSTANTS).any { it[NAME] == "INSTANCE" } }
        .forEach {
            val modifier = if (it[MODIFIERS].contains(FINAL)) PRIVATE else PROTECTED
            it[CONSTRUCTORS] = jArray(
                jObject(
                    NAME to it[NAME],
                    MODIFIERS to arrayOf(modifier)
                )
            )
        }
}
