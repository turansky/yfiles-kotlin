package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE
import java.io.File

internal object Hacks {
    val SYSTEM_FUNCTIONS = listOf("hashCode", "toString")

    fun redundantMethod(method: Method): Boolean {
        return method.name in SYSTEM_FUNCTIONS && method.parameters.isEmpty()
    }

    fun correctStaticFieldGeneric(type: String): String {
        // TODO: Check. Quick fix for generics in constants
        // One case - IListEnumerable.EMPTY
        return type.replace("<T>", "<out Any>")
    }

    fun getFunctionGenerics(className: String, name: String): String? {
        return when {
            className == "yfiles.collections.List" && name == "fromArray" -> "<T>"
            else -> null
        }
    }

    fun getReturnType(method: Method): String? {
        val className = method.fqn
        val methodName = method.name

        return when {
            className == "yfiles.algorithms.EdgeList" && methodName == "getEnumerator"
            -> "yfiles.collections.IEnumerator<${OBJECT_TYPE}>"

            className == "yfiles.algorithms.NodeList" && methodName == "getEnumerator"
            -> "yfiles.collections.IEnumerator<${OBJECT_TYPE}>"

            else -> null
        }
    }


    fun ignoreExtendedType(className: String): Boolean {
        return when (className) {
            "yfiles.lang.Exception" -> true
            else -> false
        }
    }

    fun getImplementedTypes(className: String): List<String>? {
        return when (className) {
            "yfiles.algorithms.EdgeList" -> emptyList()
            "yfiles.algorithms.NodeList" -> emptyList()
            else -> null
        }
    }

    private val ARRAY_GENERIC_CORRECTION = mapOf(
            ParameterData("yfiles.graph.GroupingSupport", "getNearestCommonAncestor", "nodes") to Types.NODE_TYPE,

            ParameterData("yfiles.input.EventRecognizers", "createAndRecognizer", "recognizers") to "yfiles.input.EventRecognizer",
            ParameterData("yfiles.input.EventRecognizers", "createOrRecognizer", "recognizers") to "yfiles.input.EventRecognizer",

            ParameterData("yfiles.input.IPortCandidateProvider", "combine", "providers") to "IPortCandidateProvider",
            ParameterData("yfiles.input.IPortCandidateProvider", "fromCandidates", "candidates") to "IPortCandidate",
            ParameterData("yfiles.input.IPortCandidateProvider", "fromShapeGeometry", "ratios") to "Number",

            ParameterData("yfiles.lang.Class", "injectInterfaces", "traits") to OBJECT_TYPE,

            ParameterData("yfiles.lang.delegate", "createDelegate", "functions") to "yfiles.lang.delegate",
            ParameterData("yfiles.lang.delegate", "dynamicInvoke", "args") to OBJECT_TYPE,

            ParameterData("yfiles.lang.Class", "injectInterfaces", "interfaces") to "Interface",

            ParameterData("yfiles.view.CanvasComponent", "schedule", "args") to OBJECT_TYPE
    )

    fun addComparisonClass(sourceDir: File) {
        sourceDir.resolve("system").mkdir()
        sourceDir.resolve("system/Comparison.kt")
                .writeText("package system\n\ntypealias Comparison<T> = (T, T) -> Number", DEFAULT_CHARSET)
    }

    fun getPropertyType(className: String, propertyName: String): String? {
        return when {
            className == "yfiles.seriesparallel.SeriesParallelLayoutData" && propertyName == "outEdgeComparers"
            -> "yfiles.layout.ItemMapping<yfiles.graph.INode, system.Comparison<yfiles.graph.IEdge>>"

            className == "yfiles.tree.TreeLayoutData" && propertyName == "outEdgeComparers"
            -> "yfiles.layout.ItemMapping<yfiles.graph.INode, system.Comparison<yfiles.graph.IEdge>>"

            else -> null
        }
    }

    fun getParameterType(method: MethodBase, parameter: Parameter): String? {
        if (parameter.type != "Array") {
            return null
        }

        val className = method.fqn
        val methodName = when (method) {
            is Method -> method.name
            else -> ""
        }

        val parameterName = parameter.getCorrectedName()
        val generic = ARRAY_GENERIC_CORRECTION[ParameterData(className, methodName, parameterName)]
                ?: throw IllegalArgumentException("Unable find array generic for className: '$className' and method: '$methodName' and parameter '$parameterName'")

        return "Array<$generic>"
    }

    private val CLONE_REQUIRED = listOf(
            "yfiles.geometry.Matrix",
            "yfiles.geometry.MutablePoint",
            "yfiles.geometry.MutableSize"
    )
    private val CLONE_OVERRIDE = "override fun clone(): ${OBJECT_TYPE} = definedExternally"

    fun getAdditionalContent(className: String): String {
        return when {
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
                    "override fun isHit(context: yfiles.input.IInputModeContext, location: yfiles.geometry.Point): Boolean = definedExternally")

            className == "yfiles.styles.VoidPathGeometry"
            -> lines("override fun getPath(): yfiles.geometry.GeneralPath = definedExternally",
                    "override fun getSegmentCount(): Number = definedExternally",
                    "override fun getTangent(ratio: Number): yfiles.geometry.Tangent = definedExternally",
                    "override fun getTangent(segmentIndex: Number, ratio: Number): yfiles.geometry.Tangent = definedExternally")

            else -> ""
        }
    }

    private fun lines(vararg lines: String): String {
        return lines.joinToString("\n")
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

            ParameterData("yfiles.input.GroupingNodePositionHandler", "cancelDrag", "inputModeContext") to "context",
            ParameterData("yfiles.input.GroupingNodePositionHandler", "dragFinished", "inputModeContext") to "context",
            ParameterData("yfiles.input.GroupingNodePositionHandler", "handleMove", "inputModeContext") to "context",
            ParameterData("yfiles.input.GroupingNodePositionHandler", "initializeDrag", "inputModeContext") to "context",
            ParameterData("yfiles.input.GroupingNodePositionHandler", "setCurrentParent", "inputModeContext") to "context",

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
            ParameterData("yfiles.input.ReparentStripeHandler", "reparent", "stripe") to "movedStripe",
            ParameterData("yfiles.input.StripeDropInputMode", "updatePreview", "newLocation") to "dragLocation",
            ParameterData("yfiles.multipage.IElementFactory", "createConnectorNode", "edgesIds") to "edgeIds",
            ParameterData("yfiles.router.DynamicObstacleDecomposition", "init", "partitionBounds") to "bounds",
            ParameterData("yfiles.view.StripeSelection", "isSelected", "stripe") to "item"
    )

    fun fixParameterName(method: MethodBase, parameterName: String): String? {
        return PARAMETERS_CORRECTION[ParameterData(method.fqn, method.name, parameterName)]
    }
}

private data class ParameterData(
        val className: String,
        val functionName: String,
        val parameterName: String
)