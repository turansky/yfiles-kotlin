package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.YOBJECT

internal fun applyLayoutStrictTypes(source: Source) {
    source.type("CopiedLayoutGraph")
        .flatMap(METHODS)
        .flatMap { it.optFlatMap(PARAMETERS) + it.returnsSequence() }
        .filter { it[TYPE] == JS_OBJECT }
        .forEach { it[TYPE] = YOBJECT }
}
