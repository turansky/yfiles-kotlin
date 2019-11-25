package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICLONEABLE
import com.github.turansky.yfiles.json.firstWithName
import org.json.JSONObject

internal fun applyCloneableHacks(source: Source) {
    fixClass(source)

    fixImplementedType(source)
}

private fun fixClass(source: Source) {
    source.type("ICloneable").apply {
        setSingleTypeParameter(name = "out T", bound = "$ICLONEABLE<T>")
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
                    if (type.hasCloneableSuperType(source)) {
                        remove(index)
                    } else {
                        val typeId = type.getString(J_ID)
                        put(index, "$ICLONEABLE<$typeId>")
                    }
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

private fun JSONObject.hasCloneableSuperType(source: Source): Boolean {
    if (getString(J_ID) == "yfiles.tree.DefaultNodePlacer") {
        return true
    }

    if (!has(J_IMPLEMENTS)) {
        return false
    }

    return getJSONArray(J_IMPLEMENTS)
        .asSequence()
        .map { it as String }
        .map { it.toClassName() }
        .map { source.type(it) }
        .filter { it.has(J_IMPLEMENTS) }
        .map { it.getJSONArray(J_IMPLEMENTS) }
        .flatMap { it.asSequence() }
        .map { it as String }
        .any { it.startsWith(ICLONEABLE) }
}

private fun String.toClassName(): String =
    when (this) {
        "yfiles.hierarchic.INodePlacer" -> "IHierarchicLayoutNodePlacer"
        "yfiles.tree.INodePlacer" -> "ITreeLayoutNodePlacer"
        else -> substringAfterLast(".")
    }
