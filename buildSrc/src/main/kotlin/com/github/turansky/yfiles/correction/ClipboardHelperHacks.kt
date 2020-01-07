package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICLIPBOARD_HELPER
import com.github.turansky.yfiles.IMODEL_ITEM
import com.github.turansky.yfiles.JS_OBJECT

private const val T = "T"
private const val D = "D"

internal fun applyClipboardHelperHacks(source: Source) {
    source.type("IClipboardHelper").apply {
        set(
            TYPE_PARAMETERS,
            arrayOf(
                typeParameter("in $T", IMODEL_ITEM),
                typeParameter(D, JS_OBJECT)
            )
        )

        flatMap(METHODS)
            .flatMap(PARAMETERS)
            .forEach {
                it[TYPE] = when (it[NAME]) {
                    "item" -> T
                    "userData" -> D
                    else -> return@forEach
                }
            }

        flatMap(METHODS)
            .filter { it.has(RETURNS) }
            .filter { it[RETURNS][TYPE] == JS_OBJECT }
            .forEach { it[RETURNS][TYPE] = D }
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
