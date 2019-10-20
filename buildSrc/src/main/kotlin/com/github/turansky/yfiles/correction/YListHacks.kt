package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ANY
import com.github.turansky.yfiles.JS_OBJECT

internal fun applyYListHacks(source: Source) {
    fixYList(source)
}

private fun fixYList(source: Source) {
    source.type("YList").apply {
        setSingleTypeParameter(bound = JS_OBJECT)
    }

    source.type("YNodeList")
        .addExtendsGeneric(ANY)

    source.type("EdgeList")
        .addExtendsGeneric(ANY)
}