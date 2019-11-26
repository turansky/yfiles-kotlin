package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.json.jArray
import org.json.JSONObject

internal fun applyDataHacks(source: Source) {
    fixDataMaps(source)
}

private val IDATA_MAP = "yfiles.algorithms.IDataMap"

private val MAP_INTERFACES = setOf(
    "yfiles.algorithms.IEdgeMap",
    "yfiles.algorithms.INodeMap"
)

private fun fixDataMap(source: Source) {
    source.type(IDATA_MAP.substringAfterLast("."))
        .put(
            J_TYPE_PARAMETERS,
            jArray(
                typeParameter("K", JS_OBJECT),
                typeParameter("V", JS_OBJECT)
            )
        )

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it.getString(J_TYPE) == IDATA_MAP }
        .forEach { it.put(J_TYPE, "$IDATA_MAP<*, *>") }
}

private fun fixDataMaps(source: Source) {
    MAP_INTERFACES.forEach {
        source.type(it.substringAfterLast("."))
            .setSingleTypeParameter("V", JS_OBJECT)
    }

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it.getString(J_TYPE) in MAP_INTERFACES }
        .forEach { it.put(J_TYPE, it.getString(J_TYPE) + "<*>") }

    source.type("Graph")
        .jsequence(J_PROPERTIES)
        .forEach { property ->
            val type = property.getString(J_TYPE)
            MAP_INTERFACES.find { it in type }
                ?.also { property.put(J_TYPE, type.replace(it, "$it<*>")) }
        }
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
