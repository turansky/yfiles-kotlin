package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

internal fun applyDpKeyHacks(source: Source) {
    fixClass(source)
    fixProperties(source)
    fixMethodParameters(source)
}

private val DP_KEY_BASE_CLASS = "DpKeyBase"
private val DP_KEY_BASE_KEY = "TKey"

private val DP_KEY_BASE_DECLARATION = "$DP_KEY_BASE<"

private val DP_KEY_GENERIC_MAP = mapOf(
    DP_KEY_BASE_CLASS to DP_KEY_BASE_KEY,

    "GraphDpKey" to "yfiles.algorithms.Graph",

    "NodeDpKey" to NODE,
    "EdgeDpKey" to EDGE,
    "GraphObjectDpKey" to GRAPH_OBJECT,

    "ILabelLayoutDpKey" to "yfiles.layout.ILabelLayout",
    "IEdgeLabelLayoutDpKey" to IEDGE_LABEL_LAYOUT,
    "INodeLabelLayoutDpKey" to INODE_LABEL_LAYOUT
)

private fun fixClass(source: Source) {
    source.type(DP_KEY_BASE_CLASS).apply {
        addFirstTypeParameter(DP_KEY_BASE_KEY, JS_OBJECT)

        methodParameters(
            "equalsCore",
            "other",
            { true }
        ).single()
            .updateDpKeyGeneric(TYPE, DP_KEY_BASE_KEY)
    }

    for ((className, generic) in DP_KEY_GENERIC_MAP) {
        if (className != DP_KEY_BASE_CLASS) {
            source.type(className)
                .updateDpKeyGeneric(EXTENDS, generic)
        }
    }

    source.type("DpKeyItemCollection")
        .property("dpKey")
        .updateDpKeyGeneric(TYPE, "*")
}

fun dpKeyBase(typeParameter: String): String = "yfiles.algorithms.DpKeyBase<*,$typeParameter>"
fun nodeDpKey(typeParameter: String): String = "yfiles.algorithms.NodeDpKey<$typeParameter>"
fun edgeDpKey(typeParameter: String): String = "yfiles.algorithms.EdgeDpKey<$typeParameter>"
fun labelDpKey(typeParameter: String): String = "yfiles.algorithms.ILabelLayoutDpKey<$typeParameter>"

private fun fixProperties(source: Source) {
    val typeMap = mapOf(
        "affectedNodesDpKey" to nodeDpKey(JS_BOOLEAN),
        "splitNodesDpKey" to nodeDpKey(JS_BOOLEAN),

        "centerNodesDpKey" to nodeDpKey(JS_BOOLEAN),

        "minSizeDataProviderKey" to nodeDpKey("yfiles.algorithms.YDimension"),
        "minimumNodeSizeDpKey" to nodeDpKey("yfiles.algorithms.YDimension"),

        "groupNodeInsetsDPKey" to nodeDpKey("yfiles.algorithms.Insets"),
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

private fun fixMethodParameters(source: Source) {
    source.types("IMapperRegistry", "MapperRegistry")
        .flatMap(METHODS)
        .forEach { method ->
            method.flatMap(PARAMETERS)
                .filter { it[NAME] == "tag" }
                .filter { it[TYPE] == JS_OBJECT }
                .forEach { it[TYPE] = dpKeyBase(if (method.has(TYPE_PARAMETERS)) "V" else "*") }
        }

    source.type("WeightedLayerer").also {
        sequenceOf(
            it.flatMap(CONSTRUCTORS)
                .flatMap(PARAMETERS)
                .single { it[NAME] == "key" },
            it[PROPERTIES]["key"]
        ).forEach { it[TYPE] = edgeDpKey(JS_INT) }
    }

    source.type("LabelingBase")
        .flatMap(METHODS)
        .filter { it[NAME] == "label" }
        .flatMap(PARAMETERS)
        .single { it[NAME] == "key" }
        .set(TYPE, labelDpKey(JS_BOOLEAN))
}

private fun JSONObject.updateDpKeyGeneric(
    field: JStringKey,
    generic: String
) {
    val value = get(field)
    require(value.startsWith(DP_KEY_BASE_DECLARATION))
    set(field, value.replace(DP_KEY_BASE_DECLARATION, "$DP_KEY_BASE_DECLARATION$generic,"))
}
