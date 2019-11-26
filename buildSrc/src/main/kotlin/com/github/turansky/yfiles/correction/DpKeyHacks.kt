package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.EDGE
import com.github.turansky.yfiles.GRAPH_OBJECT
import com.github.turansky.yfiles.NODE
import org.json.JSONObject

internal fun applyDpKeyHacks(source: Source) {
    fixClass(source)
}

private val DP_KEY_BASE = "DpKeyBase"
private val DP_KEY_BASE_KEY = "TKey"

private val DP_KEY_BASE_DECLARATION = "yfiles.algorithms.DpKeyBase<"

private val DP_KEY_GENERIC_MAP = mapOf(
    DP_KEY_BASE to "TKey",

    "GraphDpKey" to "yfiles.algorithms.Graph",

    "NodeDpKey" to NODE,
    "EdgeDpKey" to EDGE,
    "GraphObjectDpKey" to GRAPH_OBJECT,

    "ILabelLayoutDpKey" to "yfiles.layout.ILabelLayout",
    "IEdgeLabelLayoutDpKey" to "yfiles.layout.IEdgeLabelLayout",
    "INodeLabelLayoutDpKey" to "yfiles.layout.INodeLabelLayout"
)

private fun fixClass(source: Source) {
    source.type(DP_KEY_BASE).apply {
        addFirstTypeParameter(DP_KEY_BASE_KEY)

        methodParameters(
            "equalsCore",
            "other",
            { true }
        ).single()
            .updateDpKeyGeneric(J_TYPE, DP_KEY_BASE_KEY)
    }

    for ((className, generic) in DP_KEY_GENERIC_MAP) {
        if (className != DP_KEY_BASE) {
            source.type(className)
                .updateDpKeyGeneric(J_EXTENDS, generic)
        }
    }

    source.type("DpKeyItemCollection")
        .property("dpKey")
        .updateDpKeyGeneric(J_TYPE, "*")
}

private fun JSONObject.updateDpKeyGeneric(
    field: String,
    generic: String
) {
    val value = getString(field)
    require(value.startsWith(DP_KEY_BASE_DECLARATION))
    put(field, value.replace(DP_KEY_BASE_DECLARATION, "$DP_KEY_BASE_DECLARATION$generic,"))
}
