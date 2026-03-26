package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY

internal fun applyContextMenuFixes(source: Source) {
    source.types("PopulateContextMenuEventArgs")
        .optFlatMap(PROPERTIES)
        .filter { it[TYPE] == "ContextMenuContent" }
        .forEach { it[TYPE] = JS_ANY }
}