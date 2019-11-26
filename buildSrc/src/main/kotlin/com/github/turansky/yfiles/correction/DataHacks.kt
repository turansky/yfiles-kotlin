package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_OBJECT
import org.json.JSONObject

internal fun applyDataHacks(source: Source) {
    fixClass(source)
}

private val MAP_INTERFACES = setOf(
    "yfiles.algorithms.IEdgeMap",
    "yfiles.algorithms.INodeMap"
)

private fun fixClass(source: Source) {
    MAP_INTERFACES.forEach {
        source.type(it.substringAfterLast("."))
            .setSingleTypeParameter("V", JS_OBJECT)
    }

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it.getString(J_TYPE) in MAP_INTERFACES }
        .forEach { it.put(J_TYPE, it.getString(J_TYPE) + "<*>") }
}

fun JSONObject.getTypeHolders() =
    (optJsequence(J_STATIC_METHODS) + optJsequence(J_METHODS))
        .flatMap { it.optJsequence(J_PARAMETERS) + it.returnsSequence() }
        .plus(optJsequence(J_PROPERTIES))

private fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(J_RETURNS)) {
        sequenceOf(getJSONObject(J_RETURNS))
    } else {
        emptySequence()
    }
