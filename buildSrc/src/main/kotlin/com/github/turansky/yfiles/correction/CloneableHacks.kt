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
        get(METHODS)
            .firstWithName("clone")
            .get(RETURNS)
            .set(J_TYPE, "T")
    }
}

private fun fixImplementedType(source: Source) {
    source.types()
        .filter { it.has(IMPLEMENTS) }
        .forEach { type ->
            type[IMPLEMENTS].apply {
                val index = indexOf(ICLONEABLE)
                if (index != -1) {
                    if (type.hasCloneableSuperType(source)) {
                        remove(index)
                    } else {
                        val typeId = type[ID]
                        put(index, "$ICLONEABLE<$typeId>")
                    }
                }
            }
        }

    source.types()
        .filter { it.has(METHODS) }
        .filterNot { it[ID] == ICLONEABLE }
        .forEach { type ->
            type.jsequence(METHODS)
                .filter { it[J_NAME] == "clone" }
                .filterNot { it.has(PARAMETERS) }
                .map { it[RETURNS] }
                .forEach { it[J_TYPE] = type[ID] }
        }
}

private fun JSONObject.hasCloneableSuperType(source: Source): Boolean {
    if (get(ID) == "yfiles.tree.DefaultNodePlacer") {
        return true
    }

    if (!has(IMPLEMENTS)) {
        return false
    }

    return get(IMPLEMENTS)
        .asSequence()
        .map { it as String }
        .map { it.toClassName() }
        .map { source.type(it) }
        .filter { it.has(IMPLEMENTS) }
        .map { it[IMPLEMENTS] }
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
