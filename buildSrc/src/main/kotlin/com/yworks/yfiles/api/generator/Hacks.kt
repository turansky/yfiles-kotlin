package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE
import org.json.JSONObject

internal object Hacks {
    private val SYSTEM_FUNCTIONS = listOf("hashCode", "toString")

    fun redundantMethod(method: Method): Boolean {
        return method.name in SYSTEM_FUNCTIONS && method.parameters.isEmpty()
    }

    private fun JSONObject.type(id: String): JSONObject {
        val rootPackage = id.substring(0, id.indexOf("."))
        val typePackage = id.substring(0, id.lastIndexOf("."))
        return this.getJSONArray("namespaces")
            .first { it.getString("id") == rootPackage }
            .getJSONArray("namespaces")
            .first { it.getString("id") == typePackage }
            .getJSONArray("types")
            .first { it.getString("id") == id }
    }

    private fun JSONObject.methodParameters(
        methodName: String,
        parameterName: String,
        parameterFilter: (JSONObject) -> Boolean
    ): Iterable<JSONObject> {
        val result = getJSONArray("methods")
            .objects { it.getString("name") == methodName }
            .flatMap {
                it.getJSONArray("parameters")
                    .objects { it.getString("name") == parameterName }
                    .filter(parameterFilter)
            }

        if (result.isEmpty()) {
            throw IllegalArgumentException("No method parameters found for object: $this, method: $methodName, parameter: $parameterName")
        }

        return result
    }

    private fun JSONObject.addMethod(
        methodData: MethodData
    ) {
        getJSONArray("methods")
            .put(
                mutableMapOf(
                    "name" to methodData.methodName,
                    "modifiers" to listOf("public")
                )
                    .also {
                        val parameters = methodData.parameters
                        if (parameters.isNotEmpty()) {
                            it.put(
                                "parameters",
                                parameters.map {
                                    mapOf("name" to it.name, "type" to it.type)
                                }
                            )
                        }
                    }
                    .also {
                        val resultType = methodData.resultType
                        if (resultType != null) {
                            it.put("returns", mapOf("type" to resultType))
                        }
                    }
            )
    }

    fun applyHacks(source: JSONObject) {
        fixConstantGenerics(source)
        fixFunctionGenerics(source)

        fixReturnType(source)
        fixExtendedType(source)
        fixImplementedTypes(source)
        fixPropertyType(source)
        fixMethodParameterName(source)
        addMissedMethods(source)
    }

    private fun fixConstantGenerics(source: JSONObject) {
        source.type("yfiles.collections.IListEnumerable")
            .getJSONArray("constants")
            .first { it.getString("name") == "EMPTY" }
            .also {
                val type = it.getString("type")
                    .replace("<T>", "<Object>")
                it.put("type", type)
            }
    }

    private fun fixFunctionGenerics(source: JSONObject) {
        source.type("yfiles.collections.List")
            .getJSONArray("staticMethods")
            .first { it.getString("name") == "fromArray" }
            .also {
                it.put(
                    "typeparameters", jArray(
                        jObject("name" to "T")
                    )
                )
            }
    }

    private fun fixReturnType(source: JSONObject) {
        listOf("yfiles.algorithms.EdgeList", "yfiles.algorithms.NodeList")
            .map { source.type(it) }
            .forEach {
                it.getJSONArray("methods")
                    .first { it.get("name") == "getEnumerator" }
                    .getJSONObject("returns")
                    .put("type", "yfiles.collections.IEnumerator<${OBJECT_TYPE}>")
            }
    }

    private fun fixExtendedType(source: JSONObject) {
        source.type("yfiles.lang.Exception")
            .remove("extends")
    }

    private fun fixImplementedTypes(source: JSONObject) {
        listOf("yfiles.algorithms.EdgeList", "yfiles.algorithms.NodeList")
            .map { source.type(it) }
            .forEach { it.remove("implements") }
    }

    private fun fixPropertyType(source: JSONObject) {
        listOf("yfiles.seriesparallel.SeriesParallelLayoutData", "yfiles.tree.TreeLayoutData")
            .map { source.type(it) }
            .forEach {
                it.getJSONArray("properties")
                    .first { it.getString("name") == "outEdgeComparers" }
                    .put("type", "yfiles.layout.ItemMapping<yfiles.graph.INode,Comparator<yfiles.graph.IEdge>>")
            }
    }

    private val PARAMETERS_CORRECTION = mapOf(
        ParameterData("yfiles.lang.IComparable", "compareTo", "obj") to "o",
        ParameterData("yfiles.lang.TimeSpan", "compareTo", "obj") to "o",
        ParameterData("yfiles.collections.IEnumerable", "includes", "value") to "item",

        ParameterData("yfiles.algorithms.YList", "elementAt", "i") to "index",
        ParameterData("yfiles.algorithms.YList", "includes", "o") to "item",
        ParameterData("yfiles.algorithms.YList", "indexOf", "obj") to "item",
        ParameterData("yfiles.algorithms.YList", "insert", "element") to "item",
        ParameterData("yfiles.algorithms.YList", "remove", "o") to "item",

        ParameterData("yfiles.graph.DefaultGraph", "setLabelPreferredSize", "size") to "preferredSize",

        ParameterData("yfiles.layout.CopiedLayoutGraph", "getLabelLayout", "copiedNode") to "node",
        ParameterData("yfiles.layout.CopiedLayoutGraph", "getLabelLayout", "copiedEdge") to "edge",
        ParameterData("yfiles.layout.CopiedLayoutGraph", "getLayout", "copiedNode") to "node",
        ParameterData("yfiles.layout.CopiedLayoutGraph", "getLayout", "copiedEdge") to "edge",

        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "createModelParameter", "sourceNode") to "sourceLayout",
        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "createModelParameter", "targetNode") to "targetLayout",
        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "label") to "labelLayout",
        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "sourceNode") to "sourceLayout",
        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "targetNode") to "targetLayout",
        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
        ParameterData("yfiles.layout.DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

        ParameterData("yfiles.layout.FreeEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
        ParameterData("yfiles.layout.FreeEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
        ParameterData("yfiles.layout.FreeEdgeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

        ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "createModelParameter", "sourceNode") to "sourceLayout",
        ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "createModelParameter", "targetNode") to "targetLayout",
        ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
        ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
        ParameterData("yfiles.layout.SliderEdgeLabelLayoutModel", "getLabelPlacement", "para") to "parameter",

        ParameterData("yfiles.layout.INodeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",
        ParameterData("yfiles.layout.FreeNodeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

        ParameterData("yfiles.tree.NodeOrderComparer", "compare", "edge1") to "x",
        ParameterData("yfiles.tree.NodeOrderComparer", "compare", "edge2") to "y",
        ParameterData("yfiles.tree.NodeWeightComparer", "compare", "o1") to "x",
        ParameterData("yfiles.tree.NodeWeightComparer", "compare", "o2") to "y",

        ParameterData("yfiles.seriesparallel.DefaultOutEdgeComparer", "compare", "o1") to "x",
        ParameterData("yfiles.seriesparallel.DefaultOutEdgeComparer", "compare", "o2") to "y",

        ParameterData("yfiles.view.LinearGradient", "accept", "item") to "node",
        ParameterData("yfiles.view.RadialGradient", "accept", "item") to "node",

        ParameterData("yfiles.graphml.GraphMLParseValueSerializerContext", "lookup", "serviceType") to "type",
        ParameterData("yfiles.graphml.GraphMLWriteValueSerializerContext", "lookup", "serviceType") to "type",

        ParameterData("yfiles.layout.LayoutData", "apply", "layoutGraphAdapter") to "adapter",
        ParameterData("yfiles.layout.MultiStageLayout", "applyLayout", "layoutGraph") to "graph",

        ParameterData("yfiles.hierarchic.DefaultLayerSequencer", "sequenceNodeLayers", "glayers") to "layers",
        ParameterData("yfiles.hierarchic.IncrementalHintItemMapping", "provideMapperForContext", "hintsFactory") to "context",
        ParameterData("yfiles.hierarchic.LayerConstraintData", "apply", "layoutGraphAdapter") to "adapter",
        ParameterData("yfiles.hierarchic.SequenceConstraintData", "apply", "layoutGraphAdapter") to "adapter",

        ParameterData("yfiles.input.ReparentStripeHandler", "reparent", "stripe") to "movedStripe",
        ParameterData("yfiles.input.StripeDropInputMode", "updatePreview", "newLocation") to "dragLocation",

        ParameterData("yfiles.multipage.IElementFactory", "createConnectorNode", "edgesIds") to "edgeIds",
        ParameterData("yfiles.router.DynamicObstacleDecomposition", "init", "partitionBounds") to "bounds",
        ParameterData("yfiles.styles.PathBasedEdgeStyleRenderer", "isInPath", "path") to "lassoPath",
        ParameterData("yfiles.view.StripeSelection", "isSelected", "stripe") to "item"
    )

    private fun fixMethodParameterName(source: JSONObject) {
        PARAMETERS_CORRECTION.forEach { data, fixedName ->
            source.type(data.className)
                .methodParameters(data.methodName, data.parameterName, { it.getString("name") != fixedName })
                .first()
                .put("name", fixedName)
        }
    }

    private val MISSED_METHODS = listOf(
        MethodData(className = "yfiles.geometry.Matrix", methodName = "clone", resultType = OBJECT_TYPE),
        MethodData(className = "yfiles.geometry.MutablePoint", methodName = "clone", resultType = OBJECT_TYPE),
        MethodData(className = "yfiles.geometry.MutableSize", methodName = "clone", resultType = OBJECT_TYPE),

        // YList

        MethodData(
            className = "yfiles.graph.CompositeUndoUnit",
            methodName = "tryMergeUnit",
            parameters = listOf(
                MethodParameterData("unit", "IUndoUnit")
            ),
            resultType = "boolean"
        ),
        MethodData(
            className = "yfiles.graph.CompositeUndoUnit",
            methodName = "tryReplaceUnit",
            parameters = listOf(
                MethodParameterData("unit", "IUndoUnit")
            ),
            resultType = "boolean"
        )
    )

    private fun addMissedMethods(source: JSONObject) {
        MISSED_METHODS
            .forEach { data ->
                source.type(data.className)
                    .addMethod(data)
            }
    }
}

private data class ParameterData(
    val className: String,
    val methodName: String,
    val parameterName: String
)

private data class MethodData(
    val className: String,
    val methodName: String,
    val parameters: List<MethodParameterData> = emptyList(),
    val resultType: String? = null
)

private data class MethodParameterData(
    val name: String,
    val type: String
)