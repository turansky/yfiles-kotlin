package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

internal fun applyDpKeyHacks(source: Source) {
    fixClass(source)
    fixProperties(source)
}

private val DP_KEY_BASE = "DpKeyBase"
private val DP_KEY_BASE_KEY = "TKey"

private val DP_KEY_BASE_DECLARATION = "yfiles.algorithms.DpKeyBase<"

private val DP_KEY_GENERIC_MAP = mapOf(
    DP_KEY_BASE to DP_KEY_BASE_KEY,

    "GraphDpKey" to "yfiles.algorithms.Graph",

    "NodeDpKey" to NODE,
    "EdgeDpKey" to EDGE,
    "GraphObjectDpKey" to GRAPH_OBJECT,

    "ILabelLayoutDpKey" to "yfiles.layout.ILabelLayout",
    "IEdgeLabelLayoutDpKey" to IEDGE_LABEL_LAYOUT,
    "INodeLabelLayoutDpKey" to INODE_LABEL_LAYOUT
)

private fun fixClass(source: Source) {
    source.type(DP_KEY_BASE).apply {
        addFirstTypeParameter(DP_KEY_BASE_KEY, JS_OBJECT)

        methodParameters(
            "equalsCore",
            "other",
            { true }
        ).single()
            .updateDpKeyGeneric(TYPE, DP_KEY_BASE_KEY)
    }

    for ((className, generic) in DP_KEY_GENERIC_MAP) {
        if (className != DP_KEY_BASE) {
            source.type(className)
                .updateDpKeyGeneric(EXTENDS, generic)
        }
    }

    source.type("DpKeyItemCollection")
        .property("dpKey")
        .updateDpKeyGeneric(TYPE, "*")
}

fun nodeDpKey(typeParameter: String): String = "yfiles.algorithms.NodeDpKey<$typeParameter>"
fun edgeDpKey(typeParameter: String): String = "yfiles.algorithms.EdgeDpKey<$typeParameter>"
fun labelDpKey(typeParameter: String): String = "yfiles.algorithms.ILabelLayoutDpKey<$typeParameter>"

private fun fixProperties(source: Source) {
    val typeMap = mapOf(
        "affectedNodesDpKey" to nodeDpKey(JS_BOOLEAN),
        "splitNodesDpKey" to nodeDpKey(JS_BOOLEAN),

        "centerNodesDpKey" to nodeDpKey(JS_BOOLEAN),
        "minimumNodeSizeDpKey" to nodeDpKey("yfiles.algorithms.YDimension"),
        "groupNodeInsetsDpKey" to nodeDpKey("yfiles.algorithms.Insets"),

        "affectedEdgesDpKey" to edgeDpKey(JS_BOOLEAN),
        "interEdgesDpKey" to edgeDpKey(JS_BOOLEAN),
        "nonSeriesParallelEdgesDpKey" to edgeDpKey(JS_BOOLEAN),
        "nonSeriesParallelEdgeLabelSelectionKey" to edgeDpKey(JS_BOOLEAN),
        "nonTreeEdgeSelectionKey" to edgeDpKey(JS_BOOLEAN),

        "affectedLabelsDpKey" to labelDpKey(JS_BOOLEAN),
        "nonTreeEdgeLabelSelectionKey" to labelDpKey(JS_BOOLEAN)
    )

    val types = typeMap.keys
    source.types()
        .optFlatMap(PROPERTIES)
        .filter { it[NAME] in types }
        .filter { it[TYPE] == JS_ANY }
        .forEach { it[TYPE] = typeMap.getValue(it[NAME]) }
}

private fun JSONObject.updateDpKeyGeneric(
    field: JStringKey,
    generic: String
) {
    val value = get(field)
    require(value.startsWith(DP_KEY_BASE_DECLARATION))
    set(field, value.replace(DP_KEY_BASE_DECLARATION, "$DP_KEY_BASE_DECLARATION$generic,"))
}
