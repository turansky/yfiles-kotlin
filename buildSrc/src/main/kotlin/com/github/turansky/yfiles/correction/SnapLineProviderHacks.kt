package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IEDGE
import com.github.turansky.yfiles.IMODEL_ITEM
import com.github.turansky.yfiles.INODE
import org.json.JSONObject

private const val ISNAP_LINE_PROVIDER = "yfiles.input.ISnapLineProvider"

internal fun applySnapLineProviderHacks(source: Source) {
    source.type("ISnapLineProvider").apply {
        setSingleTypeParameter("in T", IMODEL_ITEM)

        fixItemType("T")
    }

    sequenceOf(
        "NodeSnapLineProvider" to INODE,
        "EdgeSnapLineProvider" to IEDGE
    ).forEach { (className, typeParameter) ->
        source.type(className).apply {
            get(IMPLEMENTS).apply {
                put(indexOf(ISNAP_LINE_PROVIDER), "$ISNAP_LINE_PROVIDER<$typeParameter>")
            }

            fixItemType(typeParameter)
        }
    }

    fixDecoratorProperties(source, ISNAP_LINE_PROVIDER)
}

fun JSONObject.fixItemType(type: String) {
    flatMap(METHODS)
        .flatMap(PARAMETERS)
        .filter { it[NAME] == "item" }
        .filter { it[TYPE] == IMODEL_ITEM }
        .forEach { it[TYPE] = type }
}
