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

    private fun JSONObject.methodParameters(methodName: String,
                                            parameterName: String,
                                            parameterFilter: (JSONObject) -> Boolean): Iterable<JSONObject> {
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

    fun applyHacks(source: JSONObject) {
        addComparisonClass(source)

        fixConstantGenerics(source)
        fixFunctionGenerics(source)

        fixReturnType(source)
        fixExtendedType(source)
        fixImplementedTypes(source)
        fixPropertyType(source)
        fixMethodParameterName(source)
    }

    // yfiles.api.json correction required
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

    // yfiles.api.json correction required
    private fun fixFunctionGenerics(source: JSONObject) {
        source.type("yfiles.collections.List")
                .getJSONArray("staticMethods")
                .first { it.getString("name") == "fromArray" }
                .also {
                    it.put("typeparameters", jArray(
                            jObject("name" to "T")
                    ))
                }
    }

    // yfiles.api.json correction required
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

    // yfiles.api.json correction required
    private fun fixImplementedTypes(source: JSONObject) {
        listOf("yfiles.algorithms.EdgeList", "yfiles.algorithms.NodeList")
                .map { source.type(it) }
                .forEach { it.remove("implements") }
    }

    // yfiles.api.json correction required
    private fun addComparisonClass(source: JSONObject) {
        source.getJSONObject("functionSignatures")
                .put(
                        "system.Comparison",
                        jObject(
                                "parameters" to jArray(
                                        jObject("name" to "o1", "type" to "T"),
                                        jObject("name" to "o2", "type" to "T")
                                ),
                                "typeparameters" to jArray(
                                        jObject("name" to "T")
                                ),
                                "returns" to jObject("type" to "number")
                        )
                )
    }

    // yfiles.api.json correction required
    private fun fixPropertyType(source: JSONObject) {
        listOf("yfiles.seriesparallel.SeriesParallelLayoutData", "yfiles.tree.TreeLayoutData")
                .map { source.type(it) }
                .forEach {
                    it.getJSONArray("properties")
                            .first { it.getString("name") == "outEdgeComparers" }
                            .put("type", "yfiles.layout.ItemMapping<yfiles.graph.INode,system.Comparison<yfiles.graph.IEdge>>")
                }
    }

    // yfiles.api.json correction required
    private val CLONE_REQUIRED = listOf(
            "yfiles.geometry.Matrix",
            "yfiles.geometry.MutablePoint",
            "yfiles.geometry.MutableSize"
    )

    private val CLONE_OVERRIDE = "override fun clone(): ${OBJECT_TYPE} = definedExternally"

    // yfiles.api.json correction required
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.algorithms.YList"
            -> lines("override val isReadOnly: Boolean",
                    "    get() = definedExternally",
                    "override fun add(item: ${OBJECT_TYPE}) = definedExternally")


            className in CLONE_REQUIRED
            -> CLONE_OVERRIDE

            className == "yfiles.graph.CompositeUndoUnit"
            -> lines("override fun tryMergeUnit(unit: IUndoUnit): Boolean = definedExternally",
                    "override fun tryReplaceUnit(unit: IUndoUnit): Boolean = definedExternally")

            className == "yfiles.graph.EdgePathLabelModel" || className == "yfiles.graph.EdgeSegmentLabelModel"
            -> lines("override fun findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter = definedExternally",
                    "override fun getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter> = definedExternally",
                    "override fun getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle = definedExternally")

            className == "yfiles.graph.FreeLabelModel"
            -> "override fun findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter = definedExternally"

            className == "yfiles.graph.GenericLabelModel"
            -> lines("override fun canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean = definedExternally",
                    "override fun getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter> = definedExternally",
                    "override fun convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension = definedExternally",
                    "override fun getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle = definedExternally")

            className == "yfiles.graph.GenericPortLocationModel"
            -> lines("override fun canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean = definedExternally",
                    "override fun convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension = definedExternally",
                    "override fun getEnumerator(): yfiles.collections.IEnumerator<IPortLocationModelParameter> = definedExternally")

            className == "yfiles.input.PortRelocationHandleProvider"
            -> "override fun getHandle(context: IInputModeContext, edge: yfiles.graph.IEdge, sourceHandle: Boolean): IHandle = definedExternally"

            className == "yfiles.styles.Arrow"
            -> lines("override val length: Number",
                    "    get() = definedExternally",
                    "override fun getBoundsProvider(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, directionVector: yfiles.geometry.Point): yfiles.view.IBoundsProvider = definedExternally",
                    "override fun getVisualCreator(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, direction: yfiles.geometry.Point): yfiles.view.IVisualCreator = definedExternally",
                    CLONE_OVERRIDE)

            className == "yfiles.styles.GraphOverviewSvgVisualCreator" || className == "yfiles.view.GraphOverviewCanvasVisualCreator"
            -> lines("override fun createVisual(context: yfiles.view.IRenderContext): yfiles.view.Visual = definedExternally",
                    "override fun updateVisual(context: yfiles.view.IRenderContext, oldVisual: yfiles.view.Visual): yfiles.view.Visual = definedExternally")

            className == "yfiles.view.DefaultPortCandidateDescriptor"
            -> lines("override fun createVisual(context: IRenderContext): Visual = definedExternally",
                    "override fun updateVisual(context: IRenderContext, oldVisual: Visual): Visual = definedExternally",
                    "override fun isInBox(context: yfiles.input.IInputModeContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                    "override fun isVisible(context: ICanvasContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                    "override fun getBounds(context: ICanvasContext): yfiles.geometry.Rect = definedExternally",
                    "override fun isHit(context: yfiles.input.IInputModeContext, location: yfiles.geometry.Point): Boolean = definedExternally",
                    "override fun isInPath(context: yfiles.input.IInputModeContext, lassoPath: yfiles.geometry.GeneralPath): Boolean = definedExternally"
            )

            className == "yfiles.styles.VoidPathGeometry"
            -> lines("override fun getPath(): yfiles.geometry.GeneralPath = definedExternally",
                    "override fun getSegmentCount(): Number = definedExternally",
                    "override fun getTangent(ratio: Number): yfiles.geometry.Tangent = definedExternally",
                    "override fun getTangent(segmentIndex: Number, ratio: Number): yfiles.geometry.Tangent = definedExternally")

            else -> ""
        }

        result = result.replace(": yfiles.", ": com.yworks.yfiles.")

        return result
    }

    private fun lines(vararg lines: String): String {
        return lines.joinToString("\n")
    }

    // yfiles.api.json correction required
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

    // yfiles.api.json correction required
    private fun fixMethodParameterName(source: JSONObject) {
        PARAMETERS_CORRECTION.forEach { data, fixedName ->
            source.type(data.className)
                    .methodParameters(data.functionName, data.parameterName, { it.getString("name") != fixedName })
                    .first()
                    .put("name", fixedName)
        }
    }
}

private data class ParameterData(
        val className: String,
        val functionName: String,
        val parameterName: String
)