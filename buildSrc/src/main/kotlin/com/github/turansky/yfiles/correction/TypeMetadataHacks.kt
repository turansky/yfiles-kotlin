package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.removeItem

internal fun removeTypeMetadataMethods(source: Source) {
    source.type("GraphMLIOHandler") {
        get(METHODS).removeItem(method("addTypeInformation"))
    }
}