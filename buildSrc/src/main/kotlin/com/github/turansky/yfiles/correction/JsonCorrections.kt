package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.CANBENULL
import com.github.turansky.yfiles.JS_BOOLEAN
import com.github.turansky.yfiles.JS_NUMBER
import com.github.turansky.yfiles.JS_OBJECT

internal val UNUSED_FUNCTION_SIGNATURES = setOf(
    "system.Action3",
    "system.Action4",
    "system.Func1",
    "system.Func4",
    "system.Func5",
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

internal val PARAMETERS_CORRECTION = mapOf(
    ParameterData("IComparable", "compareTo", "obj") to "o",
    ParameterData("TimeSpan", "compareTo", "obj") to "o",
    ParameterData("IEnumerable", "includes", "value") to "item",

    ParameterData("YList", "indexOf", "obj") to "item",
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
    ParameterData("MultiStageLayout", "applyLayout", "layoutGraph") to "graph",

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
    ParameterData("StripeSelection", "isSelected", "stripe") to "item"
)

internal val PARAMETERS_NULLABILITY_CORRECTION = mapOf(
    ParameterData("YList", "copyTo", "array") to false,
    ParameterData("ObservableCollection", "copyTo", "array") to false,

    ParameterData("IGraph", "addPortAt", "style") to true,
    ParameterData("ILookupDecorator", "add", "nullIsFallback") to true,
    ParameterData("ILookupDecorator", "add", "decorateNull", true) to true,

    ParameterData("CreationProperties", "get", "key") to true,
    ParameterData("CreationProperties", "set", "key") to true,

    ParameterData("TemplatePortStyleRenderer", "updateVisual", "context") to false,
    ParameterData("TemplatePortStyleRenderer", "updateVisual", "oldVisual") to true,

    ParameterData("IAnimation", "createEasedAnimation", "easeIn") to true,
    ParameterData("IAnimation", "createEasedAnimation", "easeOut") to true,
    ParameterData("FocusIndicatorManager", "getInstaller", "item") to true
)

internal val MODEL_MANAGER_ITEM_METHODS = setOf(
    "add",
    "getCanvasObjectGroup",
    "getInstaller",
    "install",
    "remove"
)

internal val BROKEN_NULLABILITY_METHODS = setOf(
    "applyLayout",
    "applyLayoutCore"
)

internal val METHOD_NULLABILITY_MAP = mapOf(
    MethodDeclaration(className = "Graph", methodName = "getDataProvider") to true,
    MethodDeclaration(className = "ViewportLimiter", methodName = "getCurrentBounds") to true,
    MethodDeclaration(className = "IEnumerable", methodName = "first") to false,

    MethodDeclaration(className = "IDataProvider", methodName = "get") to true,
    MethodDeclaration(className = "DataProviderBase", methodName = "get") to true,
    MethodDeclaration(className = "MapperDataProviderAdapter", methodName = "get") to true
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
            MethodParameterData("item", JS_OBJECT, true)
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
        methodName = "findBestParameter",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel"),
            MethodParameterData("layout", "yfiles.geometry.IOrientedRectangle")
        ),
        result = ResultData("ILabelModelParameter")
    ),
    MethodData(
        className = "EdgePathLabelModel",
        methodName = "getParameters",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel")
        ),
        result = ResultData("yfiles.collections.IEnumerable<ILabelModelParameter>")
    ),
    MethodData(
        className = "EdgePathLabelModel",
        methodName = "getGeometry",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("layoutParameter", "ILabelModelParameter")
        ),
        result = ResultData("yfiles.geometry.IOrientedRectangle")
    ),

    MethodData(
        className = "EdgeSegmentLabelModel",
        methodName = "findBestParameter",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel"),
            MethodParameterData("layout", "yfiles.geometry.IOrientedRectangle")
        ),
        result = ResultData("ILabelModelParameter")
    ),
    MethodData(
        className = "EdgeSegmentLabelModel",
        methodName = "getParameters",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel")
        ),
        result = ResultData("yfiles.collections.IEnumerable<ILabelModelParameter>")
    ),
    MethodData(
        className = "EdgeSegmentLabelModel",
        methodName = "getGeometry",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("layoutParameter", "ILabelModelParameter")
        ),
        result = ResultData("yfiles.geometry.IOrientedRectangle")
    ),

    MethodData(
        className = "FreeLabelModel",
        methodName = "findBestParameter",
        parameters = listOf(
            MethodParameterData("label", "ILabel"),
            MethodParameterData("model", "ILabelModel"),
            MethodParameterData("layout", "yfiles.geometry.IOrientedRectangle")
        ),
        result = ResultData("ILabelModelParameter")
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
        result = ResultData("yfiles.collections.IEnumerable<ILabelModelParameter>")
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
            MethodParameterData("layoutParameter", "ILabelModelParameter")
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
            MethodParameterData("edge", "yfiles.graph.IEdge"),
            MethodParameterData("sourceHandle", JS_BOOLEAN)
        ),
        result = ResultData("IHandle", true)
    ),

    MethodData(
        className = "Arrow",
        methodName = "getBoundsProvider",
        parameters = listOf(
            MethodParameterData("edge", "yfiles.graph.IEdge"),
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
            MethodParameterData("edge", "yfiles.graph.IEdge"),
            MethodParameterData("atSource", JS_BOOLEAN),
            MethodParameterData("anchor", "yfiles.geometry.Point"),
            MethodParameterData("direction", "yfiles.geometry.Point")
        ),
        result = ResultData("yfiles.view.IVisualCreator")
    ),
    MethodData(className = "Arrow", methodName = "clone", result = ResultData(JS_OBJECT)),

    MethodData(
        className = "GraphOverviewSvgVisualCreator",
        methodName = "createVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext")
        ),
        result = ResultData("yfiles.view.Visual", true)
    ),
    MethodData(
        className = "GraphOverviewSvgVisualCreator",
        methodName = "updateVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext"),
            MethodParameterData("oldVisual", "yfiles.view.Visual", true)
        ),
        result = ResultData("yfiles.view.Visual", true)
    ),

    MethodData(
        className = "GraphOverviewCanvasVisualCreator",
        methodName = "createVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext")
        ),
        result = ResultData("yfiles.view.Visual", true)
    ),
    MethodData(
        className = "GraphOverviewCanvasVisualCreator",
        methodName = "updateVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext"),
            MethodParameterData("oldVisual", "yfiles.view.Visual", true)
        ),
        result = ResultData("yfiles.view.Visual", true)
    ),

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
            MethodParameterData("context", "yfiles.input.IInputModeContext"),
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
            MethodParameterData("context", "yfiles.input.IInputModeContext"),
            MethodParameterData("location", "yfiles.geometry.Point")
        ),
        result = ResultData(JS_BOOLEAN)
    ),
    MethodData(
        className = "DefaultPortCandidateDescriptor",
        methodName = "isInPath",
        parameters = listOf(
            MethodParameterData("context", "yfiles.input.IInputModeContext"),
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
        methodName = "getTangent",
        parameters = listOf(
            MethodParameterData("segmentIndex", JS_NUMBER),
            MethodParameterData("ratio", JS_NUMBER)
        ),
        result = ResultData("yfiles.geometry.Tangent", true)
    ),
    MethodData(
        className = "GraphOverviewWebGLVisualCreator",
        methodName = "createVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext")
        ),
        result = ResultData("yfiles.view.Visual", true)
    ),
    MethodData(
        className = "GraphOverviewWebGLVisualCreator",
        methodName = "updateVisual",
        parameters = listOf(
            MethodParameterData("context", "yfiles.view.IRenderContext"),
            MethodParameterData("oldVisual", "yfiles.view.Visual", true)
        ),
        result = ResultData("yfiles.view.Visual", true)
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
    MethodDeclaration(className = "ObservableCollection", methodName = "includes")
)

internal val SYSTEM_FUNCTIONS = listOf(
    "equals",
    "hashCode",
    "toString"
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