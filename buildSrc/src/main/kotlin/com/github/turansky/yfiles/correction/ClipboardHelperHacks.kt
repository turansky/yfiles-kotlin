package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICLIPBOARD_HELPER
import com.github.turansky.yfiles.IMODEL_ITEM
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.between

internal fun applyClipboardHelperHacks(source: Source) {
    val T = "T"
    val D = "D"

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

    source.types()
        .filter { it[ID].run { startsWith("yfiles.graph.") && endsWith("Decorator") } }
        .optFlatMap(PROPERTIES)
        .filter { it[TYPE].endsWith("$ICLIPBOARD_HELPER>") }
        .forEach {
            val typeParameter = between(it[TYPE], "<", ",")
            it[TYPE] = it[TYPE].replace(">", "<$typeParameter,*>>")
        }

    source.type("GraphClipboard")
        .method("getClipboardHelper")
        .apply {
            setSingleTypeParameter(bound = IMODEL_ITEM)
            firstParameter[TYPE] = "T"
            get(RETURNS).addGeneric("T,*")
        }
}
