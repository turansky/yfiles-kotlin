package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*

internal val UNUSED_FUNCTION_SIGNATURES = setOf(
    "yfiles.lang.Action3",
    "yfiles.lang.Action4",
    "yfiles.lang.Func1",
    "yfiles.lang.Func4",
    "yfiles.lang.Func5",
    "yfiles.input.HitTestableHandler",
    "yfiles.input.KeyEventHandler"
)

internal val PROPERTY_NULLABILITY_CORRECTION = mapOf(
    PropertyDeclaration("DefaultGraph", "tag") to true,
    PropertyDeclaration("GraphWrapperBase", "tag") to true,
    PropertyDeclaration("SimpleBend", "tag") to true,
    PropertyDeclaration("SimpleEdge", "tag") to true,
    PropertyDeclaration("SimpleLabel", "tag") to true,
    PropertyDeclaration("SimpleNode", "tag") to true,
    PropertyDeclaration("SimplePort", "tag") to true,

    PropertyDeclaration("SimpleBend", "owner") to true,
    PropertyDeclaration("SimpleEdge", "sourcePort") to true,
    PropertyDeclaration("SimpleEdge", "targetPort") to true,
    PropertyDeclaration("SimpleLabel", "owner") to true,
    PropertyDeclaration("SimplePort", "owner") to true,

    PropertyDeclaration("INodeData", "firstSameLayerEdgeCell") to true,
    PropertyDeclaration("INodeData", "nodeLayoutDescriptor") to true,
    PropertyDeclaration("INodeData", "swimLaneDescriptor") to true,

    PropertyDeclaration("ClickInputMode", "inputModeContext") to true,
    PropertyDeclaration("ContextMenuInputMode", "inputModeContext") to true,
    PropertyDeclaration("CreateBendInputMode", "inputModeContext") to true,
    PropertyDeclaration("CreateEdgeInputMode", "inputModeContext") to true,
    PropertyDeclaration("DefaultPortCandidate", "candidateTag") to true,
    PropertyDeclaration("DefaultPortCandidate", "port") to true,
    PropertyDeclaration("DropInputMode", "inputModeContext") to true,
    PropertyDeclaration("FocusGuardInputMode", "inputModeContext") to true,
    PropertyDeclaration("HandleInputMode", "inputModeContext") to true,
    PropertyDeclaration("ItemHoverInputMode", "inputModeContext") to true,
    PropertyDeclaration("KeyboardInputMode", "inputModeContext") to true,
    PropertyDeclaration("LabelDropInputMode", "draggedItem") to true,
    PropertyDeclaration("LassoSelectionInputMode", "inputModeContext") to true,
    PropertyDeclaration("MarqueeSelectionInputMode", "inputModeContext") to true,
    PropertyDeclaration("MouseHoverInputMode", "inputModeContext") to true,
    PropertyDeclaration("MoveInputMode", "inputModeContext") to true,
    PropertyDeclaration("MoveViewportInputMode", "inputModeContext") to true,
    PropertyDeclaration("MultiplexingInputMode", "inputModeContext") to true,
    PropertyDeclaration("NavigationInputMode", "inputModeContext") to true,
    PropertyDeclaration("NodeDropInputMode", "draggedItem") to true,
    PropertyDeclaration("PortDropInputMode", "draggedItem") to true,
    PropertyDeclaration("ResizeStripeInputMode", "inputModeContext") to true,
    PropertyDeclaration("StripeDropInputMode", "draggedItem") to true,
    PropertyDeclaration("TapInputMode", "inputModeContext") to true,
    PropertyDeclaration("TextEditorInputMode", "inputModeContext") to true,
    PropertyDeclaration("WaitInputMode", "inputModeContext") to true
)

internal val CONSTRUCTOR_PARAMETERS_CORRECTION = mapOf(
    ConstructorParameterData("TimeSpan", "millis") to "milliseconds",

    ConstructorParameterData("SvgDefsManager", "defsElement") to "defs",
    ConstructorParameterData("LayoutGraphHider", "g") to "graph",

    ConstructorParameterData("LineSegment", "p1") to "firstEndPoint",
    ConstructorParameterData("LineSegment", "p2") to "secondEndPoint",

    ConstructorParameterData("NodeEventArgs", "oldParent") to "parent",
    ConstructorParameterData("NodeEventArgs", "oldIsGroupNode") to "isGroupNode",
    ConstructorParameterData("FoldingEdgeStateId", "sourceCollapsed") to "sourceIsCollapsed",
    ConstructorParameterData("FoldingEdgeStateId", "targetCollapsed") to "targetIsCollapsed",

    ConstructorParameterData("HandleSerializationEventArgs", "serializationType") to "sourceType",
    ConstructorParameterData("QueryTypeEventArgs", "xName") to "xmlName",
    ConstructorParameterData("XmlName", "ns") to "namespace",

    ConstructorParameterData("GivenSequenceSequencer", "c") to "sequenceComparer",

    ConstructorParameterData("EdgeCellInfo", "enterSegmentNo") to "enterSegmentIndex",
    ConstructorParameterData("OrthogonalInterval", "isVertical") to "vertical",

    ConstructorParameterData("DefaultSeriesParallelLayoutPortAssignment", "ratio") to "borderGapToPortGapRatio",
    ConstructorParameterData("DefaultTreeLayoutPortAssignment", "ratio") to "borderGapToPortGapRatio",

    ConstructorParameterData("Animator", "canvas") to "canvasComponent",
    ConstructorParameterData("ItemSelectionChangedEventArgs", "selected") to "itemSelected",
    ConstructorParameterData("KeyEventArgs", "type") to "eventType",
    ConstructorParameterData("ModelManager", "canvas") to "canvasComponent",
    ConstructorParameterData("SvgVisual", "element") to "svgElement"
)

internal val PARAMETERS_CORRECTION = mapOf(
    ParameterData("IComparable", "compareTo", "obj") to "o",
    ParameterData("TimeSpan", "compareTo", "obj") to "o",
    ParameterData("IEnumerable", "includes", "value") to "item",

    ParameterData("YList", "insert", "element") to "item",
    ParameterData("YList", "remove", "o") to "item",

    ParameterData("DefaultGraph", "setLabelPreferredSize", "size") to "preferredSize",

    ParameterData("CopiedLayoutGraph", "getLabelLayout", "copiedNode") to "node",
    ParameterData("CopiedLayoutGraph", "getLabelLayout", "copiedEdge") to "edge",
    ParameterData("CopiedLayoutGraph", "getLayout", "copiedNode") to "node",
    ParameterData("CopiedLayoutGraph", "getLayout", "copiedEdge") to "edge",

    ParameterData("DiscreteEdgeLabelLayoutModel", "createModelParameter", "sourceNode") to "sourceLayout",
    ParameterData("DiscreteEdgeLabelLayoutModel", "createModelParameter", "targetNode") to "targetLayout",
    ParameterData("DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "label") to "labelLayout",
    ParameterData("DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "sourceNode") to "sourceLayout",
    ParameterData("DiscreteEdgeLabelLayoutModel", "getLabelCandidates", "targetNode") to "targetLayout",
    ParameterData("DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
    ParameterData("DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
    ParameterData("DiscreteEdgeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

    ParameterData("FreeEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
    ParameterData("FreeEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
    ParameterData("FreeEdgeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

    ParameterData("SliderEdgeLabelLayoutModel", "createModelParameter", "sourceNode") to "sourceLayout",
    ParameterData("SliderEdgeLabelLayoutModel", "createModelParameter", "targetNode") to "targetLayout",
    ParameterData("SliderEdgeLabelLayoutModel", "getLabelPlacement", "sourceNode") to "sourceLayout",
    ParameterData("SliderEdgeLabelLayoutModel", "getLabelPlacement", "targetNode") to "targetLayout",
    ParameterData("SliderEdgeLabelLayoutModel", "getLabelPlacement", "para") to "parameter",

    ParameterData("INodeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",
    ParameterData("FreeNodeLabelLayoutModel", "getLabelPlacement", "param") to "parameter",

    ParameterData("NodeOrderComparer", "compare", "edge1") to "x",
    ParameterData("NodeOrderComparer", "compare", "edge2") to "y",
    ParameterData("NodeWeightComparer", "compare", "o1") to "x",
    ParameterData("NodeWeightComparer", "compare", "o2") to "y",

    ParameterData("DefaultOutEdgeComparer", "compare", "o1") to "x",
    ParameterData("DefaultOutEdgeComparer", "compare", "o2") to "y",

    ParameterData("LinearGradient", "accept", "item") to "node",
    ParameterData("RadialGradient", "accept", "item") to "node",

    ParameterData("GraphMLParseValueSerializerContext", "lookup", "serviceType") to "type",
    ParameterData("GraphMLWriteValueSerializerContext", "lookup", "serviceType") to "type",

    ParameterData("LayoutData", "apply", "layoutGraphAdapter") to "adapter",

    ParameterData("DefaultLayerSequencer", "sequenceNodeLayers", "glayers") to "layers",
    ParameterData("IncrementalHintItemMapping", "provideMapperForContext", "hintsFactory") to "context",
    ParameterData("LayerConstraintData", "apply", "layoutGraphAdapter") to "adapter",
    ParameterData("SequenceConstraintData", "apply", "layoutGraphAdapter") to "adapter",

    ParameterData("ReparentStripeHandler", "reparent", "stripe") to "movedStripe",
    ParameterData("StripeDropInputMode", "updatePreview", "newLocation") to "dragLocation",

    ParameterData("IElementFactory", "createConnectorNode", "edgesIds") to "edgeIds",
    ParameterData("DynamicObstacleDecomposition", "init", "partitionBounds") to "bounds",
    ParameterData("PathBasedEdgeStyleRenderer", "isInPath", "path") to "lassoPath",
    ParameterData("IArrow", "getBoundsProvider", "directionVector") to "direction",
    ParameterData("StripeSelection", "isSelected", "stripe") to "item",

    ParameterData("NodeReshapeHandleProvider", "getHandle", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "cancelDrag", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "dragFinished", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "handleMove", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "initializeDrag", "inputModeContext") to "context",

    ParameterData("GraphOverviewCanvasVisualCreator", "createVisual", "ctx") to "context",
    ParameterData("GraphOverviewCanvasVisualCreator", "updateVisual", "ctx") to "context",
    ParameterData("GraphOverviewWebGLVisualCreator", "createVisual", "ctx") to "context",
    ParameterData("GraphOverviewWebGLVisualCreator", "updateVisual", "ctx") to "context"
)

internal val PARAMETERS_NULLABILITY_CORRECTION = mapOf(
    ParameterData("LineSegment", "contains", "point") to false,
    ParameterData("Rectangle2D", "contains", "rect") to false,

    ParameterData("Edge", "opposite", "v") to false,
    ParameterData("Graph", "disposeEdgeMap", "map") to false,
    ParameterData("Graph", "disposeNodeMap", "map") to false,
    ParameterData("Graph", "sortEdges", "comparer") to false,
    ParameterData("Graph", "sortEdges", "inComparer", true) to false,
    ParameterData("Graph", "sortEdges", "outComparer", true) to false,
    ParameterData("Graph", "sortNodes", "comparer") to false,
    ParameterData("YNode", "sortInEdges", "c") to false,
    ParameterData("YNode", "sortOutEdges", "c") to false,

    ParameterData("YList", "setInfo", "value") to false,
    ParameterData("List", "binarySearch", "item") to false,

    ParameterData("IGraph", "contains", "item") to false,
    ParameterData("IGraph", "isGroupNode", "node") to false,
    ParameterData("IGraph", "setIsGroupNode", "node") to false,

    ParameterData("DefaultGraph", "contains", "item") to false,
    ParameterData("DefaultGraph", "isGroupNode", "node") to false,
    ParameterData("DefaultGraph", "setIsGroupNode", "node") to false,

    ParameterData("FilteredGraphWrapper", "contains", "item") to false,
    ParameterData("FilteredGraphWrapper", "isGroupNode", "node") to false,
    ParameterData("FilteredGraphWrapper", "setIsGroupNode", "node") to false,
    ParameterData("GraphWrapperBase", "contains", "item") to false,
    ParameterData("GraphWrapperBase", "isGroupNode", "node") to false,
    ParameterData("GraphWrapperBase", "setIsGroupNode", "node") to false,

    ParameterData("ILookupDecorator", "add", "nullIsFallback") to true,
    ParameterData("ILookupDecorator", "add", "decorateNull", true) to true,

    ParameterData("GraphClipboard", "isDummy", "item") to false,
    ParameterData("GraphModelManager", "update", "item") to false,

    ParameterData("NavigationInputMode", "enterGroup", "node") to false,
    ParameterData("NavigationInputMode", "shouldEnterGroup", "node") to false,

    ParameterData("CreationProperties", "get", "key") to true,
    ParameterData("CreationProperties", "set", "key") to true,

    ParameterData("TemplatePortStyleRenderer", "updateVisual", "context") to false,
    ParameterData("TemplatePortStyleRenderer", "updateVisual", "oldVisual") to true,

    ParameterData("IAnimation", "createEasedAnimation", "easeIn") to true,
    ParameterData("IAnimation", "createEasedAnimation", "easeOut") to true,
    ParameterData("FocusIndicatorManager", "getInstaller", "item") to true,

    ParameterData("ICanvasObjectDescriptor", "getBoundsProvider", "forUserObject") to false,
    ParameterData("ICanvasObjectDescriptor", "getHitTestable", "forUserObject") to false,
    ParameterData("ICanvasObjectDescriptor", "getVisibilityTestable", "forUserObject") to false,
    ParameterData("ICanvasObjectDescriptor", "getVisualCreator", "forUserObject") to false,

    ParameterData("DefaultPortCandidateDescriptor", "getBoundsProvider", "forUserObject") to false,
    ParameterData("DefaultPortCandidateDescriptor", "getHitTestable", "forUserObject") to false,
    ParameterData("DefaultPortCandidateDescriptor", "getVisibilityTestable", "forUserObject") to false,
    ParameterData("DefaultPortCandidateDescriptor", "getVisualCreator", "forUserObject") to false,

    ParameterData("HighlightIndicatorManager", "addHighlight", "item") to false,
    ParameterData("HighlightIndicatorManager", "removeHighlight", "item") to false,
    ParameterData("SelectionIndicatorManager", "addSelection", "item") to false,
    ParameterData("SelectionIndicatorManager", "removeSelection", "item") to false,

    ParameterData("ItemModelManager", "itemAddedHandler", "source") to false
)

internal val MODEL_MANAGER_ITEM_METHODS = setOf(
    "add",
    "getCanvasObjectGroup",
    "getInstaller",
    "install",
    "remove"
)

internal val BROKEN_NULLABILITY_METHODS = setOf(
    "applyLayoutCore"
)

internal val METHOD_NULLABILITY_MAP = mapOf(
    MethodDeclaration(className = "Geom", methodName = "calcIntersection") to true,
    MethodDeclaration(className = "ShortestPathAlgorithm", methodName = "shortestPair") to true,
    MethodDeclaration(className = "YOrientedRectangle", methodName = "intersectionPoint") to true,

    MethodDeclaration("HierarchicalClusteringResult", "getDendrogramNode") to false,

    MethodDeclaration(className = "PartitionGrid", methodName = "getPartitionGrid") to true,
    MethodDeclaration(className = "PortConstraint", methodName = "getSPC") to true,
    MethodDeclaration(className = "PortConstraint", methodName = "getTPC") to true,

    MethodDeclaration(className = "IEnumerable", methodName = "elementAt") to false,
    MethodDeclaration(className = "IEnumerable", methodName = "first") to false,
    MethodDeclaration(className = "IEnumerable", methodName = "last") to false,

    MethodDeclaration(className = "Dendrogram", methodName = "getNodeAtLevel") to true,
    MethodDeclaration(className = "Dendrogram", methodName = "getOriginalNode") to true,

    MethodDeclaration(className = "IDataProvider", methodName = "get") to true,
    MethodDeclaration(className = "DataProviderBase", methodName = "get") to true,
    MethodDeclaration(className = "MapperDataProviderAdapter", methodName = "get") to true,

    MethodDeclaration(className = "ConstantLabelCandidateDescriptorProvider", methodName = "getDescriptor") to true,
    MethodDeclaration(className = "DefaultFoldingEdgeConverter", methodName = "addFoldingEdge") to true,
    MethodDeclaration(className = "DefaultGraph", methodName = "getParent") to true,
    MethodDeclaration(className = "DescriptorWrapperLabelModel", methodName = "getDescriptor") to true,
    MethodDeclaration(className = "ExcludingFoldingEdgeConverter", methodName = "addFoldingEdge") to true,
    MethodDeclaration(className = "FilteredGraphWrapper", methodName = "getParent") to true,
    MethodDeclaration(className = "FoldingEdgeConverterBase", methodName = "addFoldingEdge") to true,
    MethodDeclaration(className = "GraphWrapperBase", methodName = "getParent") to true,
    MethodDeclaration(className = "MapperRegistry", methodName = "getMapper") to true,
    MethodDeclaration(className = "MapperRegistry", methodName = "getMapperMetadata") to true,
    MethodDeclaration(className = "MergingFoldingEdgeConverter", methodName = "addFoldingEdge") to true,

    MethodDeclaration(className = "ChildParseContext", methodName = "getCurrent") to true,
    MethodDeclaration(className = "ChildWriteContext", methodName = "getCurrent") to true,
    MethodDeclaration(className = "ChildWriteContext", methodName = "getSerializationProperty") to true,
    MethodDeclaration(className = "MapperInputHandler", methodName = "parseDataCore") to true,
    MethodDeclaration(className = "MapperOutputHandler", methodName = "getValue") to true,

    MethodDeclaration(className = "GraphElementIdAcceptor", methodName = "resolveNode") to true,
    MethodDeclaration(className = "GraphElementIdAcceptor", methodName = "resolveGraph") to true,
    MethodDeclaration(className = "GraphElementIdAcceptor", methodName = "resolvePort") to true,
    MethodDeclaration(className = "GraphElementIdAcceptor", methodName = "resolveEdge") to true,
    MethodDeclaration(className = "GraphMLParseValueSerializerContext", methodName = "getValueSerializerFor") to true,
    MethodDeclaration(className = "GraphMLWriteValueSerializerContext", methodName = "getValueSerializerFor") to true,

    MethodDeclaration(className = "IncrementalHintItemMapping", methodName = "provideMapperForContext") to true,
    MethodDeclaration(className = "NodeDropInputMode", methodName = "getDropTarget") to true,

    MethodDeclaration(className = "HierarchicLayoutCore", methodName = "createGrouping") to true,
    MethodDeclaration(className = "HierarchicLayoutCore", methodName = "createPortConstraintOptimizer") to true,
    MethodDeclaration(className = "HierarchicLayoutCore", methodName = "getAlgorithmProperty") to true,
    MethodDeclaration(className = "HierarchicLayoutCore", methodName = "getEdgeLayoutDescriptors") to true,
    MethodDeclaration(className = "HierarchicLayoutCore", methodName = "getIncrementalHints") to true,
    MethodDeclaration(className = "HierarchicLayoutCore", methodName = "getNodeLayoutDescriptors") to true,
    MethodDeclaration(className = "HierarchicLayoutCore", methodName = "getSwimLaneDescriptors") to true,
    MethodDeclaration(className = "INodeData", methodName = "getNormalizedBorderLine") to true,

    MethodDeclaration(className = "FixNodeLayoutStage", methodName = "calculateFixPoint") to true,
    MethodDeclaration(className = "LayoutGroupingSupport", methodName = "getParent") to true,
    MethodDeclaration(className = "LayoutGroupingSupport", methodName = "getRepresentative") to true,
    MethodDeclaration(className = "IIntersectionCalculator", methodName = "calculateIntersectionPoint") to true,
    MethodDeclaration(className = "PartitionGrid", methodName = "getColumn") to true,
    MethodDeclaration(className = "PartitionGrid", methodName = "getRow") to true,

    MethodDeclaration(className = "LayoutContext", methodName = "getOriginalEdge") to true,
    MethodDeclaration(className = "LayoutContext", methodName = "getOriginalNode") to true,
    MethodDeclaration(className = "LayoutContext", methodName = "getPageEdge") to true,
    MethodDeclaration(className = "LayoutContext", methodName = "getPageNode") to true,

    MethodDeclaration(className = "BevelNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "BevelNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "CollapsibleNodeStyleDecoratorRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "CollapsibleNodeStyleDecoratorRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "DefaultLabelStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "DefaultLabelStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "GeneralPathNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "GeneralPathNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "IconLabelStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "IconLabelStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "ImageNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "ImageNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "PanelNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "PanelNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "PathBasedEdgeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "PathBasedEdgeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "ShapeNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "ShapeNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "ShinyPlateNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "ShinyPlateNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "TableNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "TableNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "TemplateLabelStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "TemplateLabelStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "TemplateNodeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "TemplateNodeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "TemplatePortStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "TemplatePortStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "TemplateStripeStyleRenderer", methodName = "createVisual") to true,
    MethodDeclaration(className = "TemplateStripeStyleRenderer", methodName = "updateVisual") to true,
    MethodDeclaration(className = "GridVisualCreator", methodName = "createVisual") to true,
    MethodDeclaration(className = "GridVisualCreator", methodName = "updateVisual") to true,
    MethodDeclaration(className = "VoidVisualCreator", methodName = "createVisual") to true,
    MethodDeclaration(className = "VoidVisualCreator", methodName = "updateVisual") to true,

    MethodDeclaration(className = "GraphOverviewSvgVisualCreator", methodName = "createVisual") to true,
    MethodDeclaration(className = "GraphOverviewSvgVisualCreator", methodName = "updateVisual") to true,
    MethodDeclaration(className = "GraphOverviewCanvasVisualCreator", methodName = "createVisual") to true,
    MethodDeclaration(className = "GraphOverviewCanvasVisualCreator", methodName = "updateVisual") to true,
    MethodDeclaration(className = "GraphOverviewWebGLVisualCreator", methodName = "createVisual") to true,
    MethodDeclaration(className = "GraphOverviewWebGLVisualCreator", methodName = "updateVisual") to true,

    MethodDeclaration(className = "BevelNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "CollapsibleNodeStyleDecoratorRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "GeneralPathNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "ImageNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "PanelNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "ShapeNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "ShinyPlateNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "TableNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "TemplateNodeStyleRenderer", methodName = "getOutline") to true,
    MethodDeclaration(className = "VoidShapeGeometry", methodName = "getOutline") to true,

    MethodDeclaration(className = "ArcEdgeStyleRenderer", methodName = "getTargetArrow") to true,
    MethodDeclaration(className = "ArcEdgeStyleRenderer", methodName = "getSourceArrow") to true,
    MethodDeclaration(className = "PolylineEdgeStyleRenderer", methodName = "getTargetArrow") to true,
    MethodDeclaration(className = "PolylineEdgeStyleRenderer", methodName = "getSourceArrow") to true,

    MethodDeclaration(className = "ArcEdgeStyleRenderer", methodName = "getStroke") to true,
    MethodDeclaration(className = "PathBasedEdgeStyleRenderer", methodName = "getObstacles") to true,
    MethodDeclaration(className = "PathBasedEdgeStyleRenderer", methodName = "getPath") to true,
    MethodDeclaration(className = "PolylineEdgeStyleRenderer", methodName = "getStroke") to true,

    MethodDeclaration(className = "ColorExtension", methodName = "provideValue") to true,
    MethodDeclaration(className = "EdgeDecorationInstaller", methodName = "addCanvasObject") to true,

    MethodDeclaration(className = "EdgeFocusIndicatorInstaller", methodName = "getStroke") to true,
    MethodDeclaration(className = "EdgeFocusIndicatorInstaller", methodName = "getBendDrawing") to true,
    MethodDeclaration(className = "EdgeHighlightIndicatorInstaller", methodName = "getStroke") to true,
    MethodDeclaration(className = "EdgeHighlightIndicatorInstaller", methodName = "getBendDrawing") to true,
    MethodDeclaration(className = "EdgeSelectionIndicatorInstaller", methodName = "getStroke") to true,
    MethodDeclaration(className = "EdgeSelectionIndicatorInstaller", methodName = "getBendDrawing") to true,

    MethodDeclaration(className = "EdgeStyleDecorationInstaller", methodName = "addCanvasObject") to true,
    MethodDeclaration(className = "FocusIndicatorManager", methodName = "add") to true,
    MethodDeclaration(className = "HighlightIndicatorManager", methodName = "getInstaller") to true,
    MethodDeclaration(className = "LabelStyleDecorationInstaller", methodName = "addCanvasObject") to true,
    MethodDeclaration(className = "NodeStyleDecorationInstaller", methodName = "addCanvasObject") to true,
    MethodDeclaration(className = "OrientedRectangleIndicatorInstaller", methodName = "addCanvasObject") to true,
    MethodDeclaration(className = "PointSelectionIndicatorInstaller", methodName = "addCanvasObject") to true,
    MethodDeclaration(className = "PortStyleDecorationInstaller", methodName = "addCanvasObject") to true,
    MethodDeclaration(className = "RectangleIndicatorInstaller", methodName = "addCanvasObject") to true,

    MethodDeclaration(className = "ITreeLayoutNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "AssistantNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "CompactNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "DelegatingNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "DendrogramNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "FreeNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "GroupedNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "LayeredNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "LeafNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "LeftRightNodePlacer", methodName = "createProcessor") to true,
    MethodDeclaration(className = "NodePlacerBase", methodName = "createProcessor") to true,
    MethodDeclaration(className = "RotatableNodePlacerBase", methodName = "createProcessor") to true
)

internal val MISSED_PROPERTIES = listOf(
    PropertyData(className = "YList", propertyName = "isReadOnly", type = JS_BOOLEAN),
    PropertyData(className = "Arrow", propertyName = "length", type = JS_NUMBER)
)

internal val MISSED_METHODS = listOf(
    MethodData(className = "Matrix", methodName = "clone", result = ResultData(JS_OBJECT)),
    MethodData(className = "MutablePoint", methodName = "clone", result = ResultData(JS_OBJECT)),
    MethodData(className = "MutableSize", methodName = "clone", result = ResultData(JS_OBJECT)),
    MethodData(
        className = "MutableRectangle",
        methodName = "clone",
        result = ResultData(JS_OBJECT)
    ),
    MethodData(
        className = "OrientedRectangle",
        methodName = "clone",
        result = ResultData(JS_OBJECT)
    ),

    MethodData(
        className = "YList",
        methodName = "add",
        parameters = listOf(
            MethodParameterData("item", JS_OBJECT, false)
        )
    ),

    MethodData(
        className = "CompositeUndoUnit",
        methodName = "tryMergeUnit",
        parameters = listOf(
            MethodParameterData("unit", "IUndoUnit")
        ),
        result = ResultData(JS_BOOLEAN)
    ),
    MethodData(
        className = "CompositeUndoUnit",
        methodName = "tryReplaceUnit",
        parameters = listOf(
            MethodParameterData("unit", "IUndoUnit")
        ),
        result = ResultData(JS_BOOLEAN)
    ),

    MethodData(
        className = "EdgePathLabelModel",
        methodName = "getParameters",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel")
        ),
        result = ResultData("$IENUMERABLE<$ILABEL_MODEL_PARAMETER>")
    ),
    MethodData(
        className = "EdgePathLabelModel",
        methodName = "getGeometry",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("layoutParameter", ILABEL_MODEL_PARAMETER)
        ),
        result = ResultData("yfiles.geometry.IOrientedRectangle")
    ),

    MethodData(
        className = "EdgeSegmentLabelModel",
        methodName = "getParameters",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel")
        ),
        result = ResultData("$IENUMERABLE<$ILABEL_MODEL_PARAMETER>")
    ),
    MethodData(
        className = "EdgeSegmentLabelModel",
        methodName = "getGeometry",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("layoutParameter", ILABEL_MODEL_PARAMETER)
        ),
        result = ResultData("yfiles.geometry.IOrientedRectangle")
    ),

    MethodData(
        className = "GenericLabelModel",
        methodName = "canConvert",
        parameters = listOf(
            MethodParameterData("context", "yfiles.graphml.IWriteContext"),
            MethodParameterData("value", JS_OBJECT)
        ),
        result = ResultData(JS_BOOLEAN)
    ),
    MethodData(
        className = "GenericLabelModel",
        methodName = "getParameters",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel")
        ),
        result = ResultData("$IENUMERABLE<$ILABEL_MODEL_PARAMETER>")
    ),
    MethodData(
        className = "GenericLabelModel",
        methodName = "convert",
        parameters = listOf(
            MethodParameterData("context", "yfiles.graphml.IWriteContext"),
            MethodParameterData("value", JS_OBJECT)
        ),
        result = ResultData("yfiles.graphml.MarkupExtension")
    ),
    MethodData(
        className = "GenericLabelModel",
        methodName = "getGeometry",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("layoutParameter", ILABEL_MODEL_PARAMETER)
        ),
        result = ResultData("yfiles.geometry.IOrientedRectangle")
    ),

    MethodData(
        className = "GenericPortLocationModel",
        methodName = "canConvert",
        parameters = listOf(
            MethodParameterData("context", "yfiles.graphml.IWriteContext"),
            MethodParameterData("value", JS_OBJECT)
        ),
        result = ResultData(JS_BOOLEAN)
    ),
    MethodData(
        className = "GenericPortLocationModel",
        methodName = "convert",
        parameters = listOf(
            MethodParameterData("context", "yfiles.graphml.IWriteContext"),
            MethodParameterData("value", JS_OBJECT)
        ),
        result = ResultData("yfiles.graphml.MarkupExtension")
    ),
    MethodData(
        className = "GenericPortLocationModel",
        methodName = "getEnumerator",
        result = ResultData("yfiles.collections.IEnumerator<IPortLocationModelParameter>")
    ),

    MethodData(
        className = "PortRelocationHandleProvider",
        methodName = "getHandle",
        parameters = listOf(
            MethodParameterData("context", "IInputModeContext"),
            MethodParameterData("edge", IEDGE),
            MethodParameterData("sourceHandle", JS_BOOLEAN)
        ),
        result = ResultData("IHandle", true)
    ),

    MethodData(
        className = "Arrow",
        methodName = "getBoundsProvider",
        parameters = listOf(
            MethodParameterData("edge", IEDGE),
            MethodParameterData("atSource", JS_BOOLEAN),
            MethodParameterData("anchor", "yfiles.geometry.Point"),
            MethodParameterData("direction", "yfiles.geometry.Point")
        ),
        result = ResultData("yfiles.view.IBoundsProvider")
    ),
    MethodData(
        className = "Arrow",
        methodName = "getVisualCreator",
        parameters = listOf(
            MethodParameterData("edge", IEDGE),
            MethodParameterData("atSource", JS_BOOLEAN),
            MethodParameterData("anchor", "yfiles.geometry.Point"),
            MethodParameterData("direction", "yfiles.geometry.Point")
        ),
        result = ResultData("yfiles.view.IVisualCreator")
    ),
    MethodData(className = "Arrow", methodName = "clone", result = ResultData(JS_OBJECT)),

    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "createVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext")
        ),
        result = ResultData("yfiles.view.Visual", true)
    ),
    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "updateVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext"),
            MethodParameterData("oldVisual", "yfiles.view.Visual", true)
        ),
        result = ResultData("yfiles.view.Visual", true)
    ),
    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "isInBox",
        parameters = listOf(
            MethodParameterData("context", IINPUT_MODE_CONTEXT),
            MethodParameterData("rectangle", "yfiles.geometry.Rect")
        ),
        result = ResultData(JS_BOOLEAN)
    ),
    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "isVisible",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.ICanvasContext"),
            MethodParameterData("rectangle", "yfiles.geometry.Rect")
        ),
        result = ResultData(JS_BOOLEAN)
    ),
    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "getBounds",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.ICanvasContext")
        ),
        result = ResultData("yfiles.geometry.Rect")
    ),
    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "isHit",
        parameters = listOf(
            MethodParameterData("context", IINPUT_MODE_CONTEXT),
            MethodParameterData("location", "yfiles.geometry.Point")
        ),
        result = ResultData(JS_BOOLEAN)
    ),
    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "isInPath",
        parameters = listOf(
            MethodParameterData("context", IINPUT_MODE_CONTEXT),
            MethodParameterData("lassoPath", "yfiles.geometry.GeneralPath")
        ),
        result = ResultData(JS_BOOLEAN)
    ),

    MethodData(
        className = "VoidPathGeometry",
        methodName = "getPath",
        result = ResultData("yfiles.geometry.GeneralPath", true)
    ),
    MethodData(
        className = "VoidPathGeometry",
        methodName = "getSegmentCount",
        result = ResultData(JS_NUMBER)
    ),
    MethodData(
        className = "VoidPathGeometry",
        methodName = "getTangent",
        parameters = listOf(
            MethodParameterData("ratio", JS_NUMBER)
        ),
        result = ResultData("yfiles.geometry.Tangent", true)
    ),
    MethodData(
        className = "VoidPathGeometry",
        methodName = "getTangentForSegment",
        parameters = listOf(
            MethodParameterData("segmentIndex", JS_NUMBER),
            MethodParameterData("ratio", JS_NUMBER)
        ),
        result = ResultData("yfiles.geometry.Tangent", true)
    ),
    MethodData(
        className = "NodeLabelModelStripeLabelModelAdapter",
        methodName = "createDefaultParameter",
        result = ResultData(ILABEL_MODEL_PARAMETER)
    )
)

internal val DUPLICATED_PROPERTIES = listOf(
    PropertyDeclaration(className = "YList", propertyName = "size"),

    PropertyDeclaration(className = "ResultItemCollection", propertyName = "size"),
    PropertyDeclaration(className = "ResultItemMapping", propertyName = "size"),

    PropertyDeclaration(className = "ICollection", propertyName = "size"),
    PropertyDeclaration(className = "IListEnumerable", propertyName = "size"),
    PropertyDeclaration(className = "List", propertyName = "size"),
    PropertyDeclaration(className = "ListEnumerable", propertyName = "size"),
    PropertyDeclaration(className = "HashMap", propertyName = "size"),
    PropertyDeclaration(className = "ObservableCollection", propertyName = "size"),

    PropertyDeclaration(className = "MutableRectangle", propertyName = "isEmpty"),
    PropertyDeclaration(className = "Rect", propertyName = "bottomLeft"),
    PropertyDeclaration(className = "Rect", propertyName = "bottomRight"),
    PropertyDeclaration(className = "Rect", propertyName = "center"),
    PropertyDeclaration(className = "Rect", propertyName = "isEmpty"),
    PropertyDeclaration(className = "Rect", propertyName = "maxX"),
    PropertyDeclaration(className = "Rect", propertyName = "maxY"),
    PropertyDeclaration(className = "Rect", propertyName = "topLeft"),
    PropertyDeclaration(className = "Rect", propertyName = "topRight"),

    PropertyDeclaration(className = "DefaultGraph", propertyName = "undoEngineEnabled"),

    PropertyDeclaration(className = "DefaultSelectionModel", propertyName = "size"),
    PropertyDeclaration(className = "GraphSelection", propertyName = "size"),
    PropertyDeclaration(className = "ISelectionModel", propertyName = "size"),
    PropertyDeclaration(className = "StripeSelection", propertyName = "size")
)

internal val DUPLICATED_METHODS = listOf(
    MethodDeclaration(className = "YList", methodName = "elementAt"),
    MethodDeclaration(className = "YList", methodName = "includes"),
    MethodDeclaration(className = "YList", methodName = "toArray"),

    MethodDeclaration(className = "ICollection", methodName = "includes"),
    MethodDeclaration(className = "List", methodName = "includes"),
    MethodDeclaration(className = "List", methodName = "toArray"),
    MethodDeclaration(className = "HashMap", methodName = "includes"),
    MethodDeclaration(className = "ObservableCollection", methodName = "includes"),

    MethodDeclaration(className = "YRectangle", methodName = "compareTo")
)

internal val SYSTEM_FUNCTIONS = listOf(
    "equals",
    "hashCode",
    "toString"
)

internal data class ConstructorParameterData(
    val className: String,
    val parameterName: String
)

internal data class ParameterData(
    val className: String,
    val methodName: String,
    val parameterName: String,
    val last: Boolean = false
)

internal data class PropertyDeclaration(
    val className: String,
    val propertyName: String
)

internal data class PropertyData(
    val className: String,
    val propertyName: String,
    val type: String
)

internal data class MethodDeclaration(
    val className: String,
    val methodName: String
)

internal data class MethodData(
    val className: String,
    val methodName: String,
    val parameters: List<MethodParameterData> = emptyList(),
    val result: ResultData? = null
)

internal data class MethodParameterData(
    val name: String,
    val type: String,
    private val nullable: Boolean = false
) {
    val modifiers = if (nullable) setOf(CANBENULL) else emptySet()
}

internal data class ResultData(
    val type: String,
    private val nullable: Boolean = false
) {
    val modifiers = if (nullable) setOf(CANBENULL) else emptySet()
}
