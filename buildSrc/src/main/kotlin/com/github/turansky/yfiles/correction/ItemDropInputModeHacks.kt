package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.VARARGS
import com.github.turansky.yfiles.json.removeItem

internal fun applyItemDropInputModeHacks(source: Source) {
    source.type("ItemDropInputMode") {
       flatMap(CONSTRUCTORS)
           .flatMap(PARAMETERS)
           .first()[MODIFIERS].removeItem(VARARGS)
    }
}