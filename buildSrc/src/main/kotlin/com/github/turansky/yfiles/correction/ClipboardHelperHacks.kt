package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICLIPBOARD_HELPER
import com.github.turansky.yfiles.IMODEL_ITEM
import com.github.turansky.yfiles.JS_ANY

private const val T = "T"
private const val D = "D"

internal fun applyClipboardHelperHacks(source: Source) {
    source.type("IClipboardHelper") {
        set(
            TYPE_PARAMETERS,
            arrayOf(
                typeParameter("in $T", IMODEL_ITEM),
                typeParameter(D)
            )
        )

        flatMap(METHODS)
            .flatMap(PARAMETERS)
            .forEach {
                when (it[NAME]) {
                    "item" -> it[TYPE] = T
                    "userData" -> {
                        it[TYPE] = D
                        it.changeNullability(false)
                    }
                }
            }

        flatMap(METHODS)
            .filter { it.has(RETURNS) }
            .filter { it[RETURNS][TYPE] == JS_ANY }
            .forEach {
                it[RETURNS][TYPE] = D
                it.changeNullability(false)
            }
    }

    fixDecoratorProperties(source, ICLIPBOARD_HELPER, true)

    source.type("GraphClipboard")
        .method("getClipboardHelper")
        .apply {
            setSingleTypeParameter(bound = IMODEL_ITEM)
            firstParameter[TYPE] = "T"
            get(RETURNS).addGeneric("T,*")
        }
}
