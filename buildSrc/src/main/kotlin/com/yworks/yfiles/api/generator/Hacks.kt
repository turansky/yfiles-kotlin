package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE

internal object Hacks {
    val SYSTEM_FUNCTIONS = listOf("hashCode", "toString")

    fun redundantMethod(method: JMethod): Boolean {
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

    val LAYOUT_GRAPH_CLASSES = listOf(
            "yfiles.layout.CopiedLayoutGraph",
            "yfiles.layout.DefaultLayoutGraph",
            "yfiles.layout.LayoutGraph"
    )

    fun getReturnType(method: JMethod, type: String): String? {
        val className = method.fqn
        val methodName = method.name

        when {
            className == "yfiles.algorithms.EdgeList" && methodName == "getEnumerator" -> return "yfiles.collections.IEnumerator<${OBJECT_TYPE}>"
            className == "yfiles.algorithms.NodeList" && methodName == "getEnumerator" -> return "yfiles.collections.IEnumerator<${OBJECT_TYPE}>"
        }

        if (type != "Array") {
            return null
        }

        val generic = when {
            className == "yfiles.collections.List" && methodName == "toArray" -> "T"
            className == "yfiles.algorithms.Dendrogram" && methodName == "getClusterNodes" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.EdgeList" && methodName == "toEdgeArray" -> "yfiles.algorithms.Edge"
            className == "yfiles.algorithms.Graph" && methodName == "getEdgeArray" -> "yfiles.algorithms.Edge"
            className == "yfiles.algorithms.Graph" && methodName == "getNodeArray" -> "yfiles.algorithms.Node"
            className == "yfiles.algorithms.NodeList" && methodName == "toNodeArray" -> "yfiles.algorithms.Node"
            className == "yfiles.algorithms.PlanarEmbedding" && methodName == "getDarts" -> "yfiles.algorithms.Dart"
            className == "yfiles.algorithms.YList" && methodName == "toArray" -> OBJECT_TYPE
            className == "yfiles.algorithms.YPointPath" && methodName == "toArray" -> "yfiles.algorithms.YPoint"
            className == "yfiles.collections.IEnumerable" && methodName == "toArray" -> "T"
            className == "yfiles.lang.Class" && methodName == "getAttributes" -> "yfiles.lang.Attribute"
            className == "yfiles.lang.Class" && methodName == "getProperties" -> "yfiles.lang.PropertyInfo"
            className == "yfiles.lang.PropertyInfo" && methodName == "getAttributes" -> "yfiles.lang.Attribute"
            className in LAYOUT_GRAPH_CLASSES && methodName == "getLabelLayout" -> {
                when (method.parameters.first().getCorrectedName()) {
                    "node" -> "yfiles.layout.INodeLabelLayout"
                    "edge" -> "yfiles.layout.IEdgeLabelLayout"
                    else -> null // TODO: throw error
                }
            }
            className == "yfiles.layout.LayoutGraphAdapter" && methodName == "getEdgeLabelLayout" -> "yfiles.layout.IEdgeLabelLayout"
            className == "yfiles.layout.LayoutGraphAdapter" && methodName == "getNodeLabelLayout" -> "yfiles.layout.INodeLabelLayout"
            className == "yfiles.layout.TableLayoutConfigurator" && methodName == "getColumnLayout" -> "Number"
            className == "yfiles.layout.TableLayoutConfigurator" && methodName == "getRowLayout" -> "Number"
            className == "yfiles.router.EdgeInfo" && methodName == "calculateLineSegments" -> "yfiles.algorithms.LineSegment"
            className == "yfiles.tree.TreeLayout" && methodName == "getRootsArray" -> "yfiles.algorithms.Node"

            className == "yfiles.algorithms.Bfs" && methodName == "getLayers" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.Cursors" && methodName == "toArray" -> OBJECT_TYPE
            className == "yfiles.algorithms.GraphConnectivity" && methodName == "biconnectedComponents" -> "yfiles.algorithms.EdgeList"
            className == "yfiles.algorithms.GraphConnectivity" && methodName == "connectedComponents" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.GraphConnectivity" && methodName == "stronglyConnectedComponents" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.GraphConnectivity" && methodName == "toEdgeListArray" -> "yfiles.algorithms.EdgeList"
            className == "yfiles.algorithms.GraphConnectivity" && methodName == "toNodeListArray" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.IndependentSets" && methodName == "getIndependentSets" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.Paths" && methodName == "findAllChains" -> "yfiles.algorithms.EdgeList"
            className == "yfiles.algorithms.Paths" && methodName == "findAllPaths" -> "yfiles.algorithms.EdgeList"
            className == "yfiles.algorithms.Paths" && methodName == "findAllPaths" -> "yfiles.algorithms.EdgeList"
            className == "yfiles.algorithms.ShortestPaths" && methodName == "shortestPair" -> "yfiles.algorithms.EdgeList"
            className == "yfiles.algorithms.ShortestPaths" && methodName == "uniformCost" -> "Number"
            className == "yfiles.algorithms.Sorting" && methodName == "sortNodesByDegree" -> "yfiles.algorithms.Node"
            className == "yfiles.algorithms.Sorting" && methodName == "sortNodesByIntKey" -> "yfiles.algorithms.Node"
            className == "yfiles.algorithms.Trees" && methodName == "getTreeEdges" -> "yfiles.algorithms.EdgeList"
            className == "yfiles.algorithms.Trees" && methodName == "getTreeNodes" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.Trees" && methodName == "getUndirectedTreeNodes" -> "yfiles.algorithms.NodeList"
            className == "yfiles.algorithms.YOrientedRectangle" && methodName == "calcPoints" -> "YPoint"
            className == "yfiles.algorithms.YOrientedRectangle" && methodName == "calcPointsInDouble" -> "Number"
            className == "yfiles.lang.delegate" && methodName == "getInvocationList" -> "yfiles.lang.delegate"
            className == "yfiles.router.BusRepresentations" && methodName == "toEdgeLists" -> "yfiles.algorithms.EdgeList"

            else -> throw IllegalArgumentException("Unable find array generic for className: '$className' and method: '$methodName'")
        }

        return "Array<$generic>"
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

    fun getPropertyType(property: JProperty): String? {
        if (property.type != "Array") {
            return null
        }

        val className = property.fqn
        val name = property.name
        val generic = when {
            className == "yfiles.algorithms.Graph" && name == "dataProviderKeys" -> OBJECT_TYPE
            className == "yfiles.algorithms.Graph" && name == "registeredEdgeMaps" -> "yfiles.algorithms.IEdgeMap"
            className == "yfiles.algorithms.Graph" && name == "registeredNodeMaps" -> "yfiles.algorithms.INodeMap"
            className == "yfiles.geometry.Matrix" && name == "elements" -> "Number"
            className == "yfiles.graphml.GraphMLAttribute" && name == "singletonContainers" -> Types.CLASS_TYPE
            className == "yfiles.input.GraphInputMode" && name == "clickHitTestOrder" -> "yfiles.graph.GraphItemTypes"
            className == "yfiles.input.GraphInputMode" && name == "doubleClickHitTestOrder" -> "yfiles.graph.GraphItemTypes"
            className == "yfiles.layout.LayoutGraphAdapter" && name == "dataProviderKeys" -> OBJECT_TYPE
            className == "yfiles.view.DragDropItem" && name == "types" -> "String"
            else -> throw IllegalArgumentException("Unable find array generic for className: '$className' and property: '$name'")
        }

        return "Array<$generic>"
    }

    fun getParameterType(method: JMethodBase, parameter: JParameter): String? {
        val className = method.fqn
        val methodName = when (method) {
            is JConstructor -> "constructor"
            is JMethod -> method.name
            else -> ""
        }
        val parameterName = parameter.getCorrectedName()

        if (className == "yfiles.view.IAnimation" && methodName == "createGraphAnimation" && parameterName == "targetBendLocations") {
            return "yfiles.collections.IMapper<yfiles.graph.IEdge,Array<yfiles.geometry.IPoint>>"
        }

        if (parameter.type != "Array") {
            return null
        }

        val generic = when {
            className in LAYOUT_GRAPH_CLASSES && methodName == "setLabelLayout" && parameterName == "layout" -> {
                when (method.parameters.first().getCorrectedName()) {
                    "node" -> "yfiles.layout.INodeLabelLayout"
                    "edge" -> "yfiles.layout.IEdgeLabelLayout"
                    else -> null // TODO: throw error
                }
            }
            else -> ARRAY_GENERIC_CORRECTION[ParameterData(className, methodName, parameterName)] ?: throw IllegalArgumentException("Unable find array generic for className: '$className' and method: '$methodName' and parameter '$parameterName'")
        }
        return "Array<$generic>"
    }

    val CLONE_REQUIRED = listOf(
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

    fun fixParameterName(method: JMethodBase, parameterName: String): String? {
        return PARAMETERS_CORRECTION[ParameterData(method.fqn, method.name, parameterName)]
    }
}

private data class ParameterData(
        val className: String,
        val functionName: String,
        val parameterName: String
)