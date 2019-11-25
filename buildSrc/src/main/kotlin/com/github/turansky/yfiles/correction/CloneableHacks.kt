package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICLONEABLE
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.json.firstWithName

internal fun applyCloneableHacks(source: Source) {
    fixClass(source)

    fixImplementedType(source)
}

private fun fixClass(source: Source) {
    source.type("ICloneable").apply {
        setSingleTypeParameter(name = "out T", bound = JS_OBJECT)
        getJSONArray(J_METHODS)
            .firstWithName("clone")
            .getJSONObject(J_RETURNS)
            .put(J_TYPE, "T")
    }
}

private fun fixImplementedType(source: Source) {
    source.types()
        .filter { it.has(J_IMPLEMENTS) }
        .forEach { type ->
            type.getJSONArray(J_IMPLEMENTS).apply {
                val index = indexOf(ICLONEABLE)
                if (index != -1) {
                    val typeId = type.getString(J_ID)
                    put(index, "$ICLONEABLE<$typeId>")
                }
            }
        }

    source.types()
        .filter { it.has(J_METHODS) }
        .filterNot { it.getString(J_ID) == ICLONEABLE }
        .forEach { type ->
            type.jsequence(J_METHODS)
                .filter { it.getString(J_NAME) == "clone" }
                .filterNot { it.has(J_PARAMETERS) }
                .map { it.getJSONObject(J_RETURNS) }
                .forEach { it.put(J_TYPE, type.getString(J_ID)) }
        }
}
