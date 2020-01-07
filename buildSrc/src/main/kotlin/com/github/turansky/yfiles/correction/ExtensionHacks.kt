package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ABSTRACT

internal fun applyExtensionHacks(source: Source) {
    source.type("IFoldingView")
        .property("manager")
        .get(MODIFIERS)
        .put(ABSTRACT)
}
