package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.CANBENULL

internal val UNUSED_FUNCTION_SIGNATURES = setOf(
//    "yfiles.lang.Action3",
    "yfiles.lang.Action4",
//    "yfiles.lang.Func1",
//    "yfiles.lang.Func4",
    "yfiles.lang.Func5",
    "yfiles.input.HitTestableHandler",
    "yfiles.input.KeyEventHandler"
)

internal val PROPERTY_NULLABILITY_CORRECTION = mapOf(
//    PropertyDeclaration("DefaultGraph", "tag") to true,
//    PropertyDeclaration("GraphWrapperBase", "tag") to true,
//    PropertyDeclaration("SimpleBend", "tag") to true,
//    PropertyDeclaration("SimpleEdge", "tag") to true,
//    PropertyDeclaration("SimpleLabel", "tag") to true,
//    PropertyDeclaration("SimpleNode", "tag") to true,
//    PropertyDeclaration("SimplePort", "tag") to true,

//    PropertyDeclaration("SimpleBend", "owner") to true,
//    PropertyDeclaration("SimpleEdge", "sourcePort") to true,
//    PropertyDeclaration("SimpleEdge", "targetPort") to true,
//    PropertyDeclaration("SimpleLabel", "owner") to true,
//    PropertyDeclaration("SimplePort", "owner") to true,

    PropertyDeclaration("ICursor", "current") to false,
    PropertyDeclaration("LabelDropInputMode", "draggedItem") to true,
    PropertyDeclaration("NodeDropInputMode", "draggedItem") to true,
    PropertyDeclaration("PortDropInputMode", "draggedItem") to true,
    PropertyDeclaration("StripeDropInputMode", "draggedItem") to true,
)

internal val CONSTRUCTOR_PARAMETERS_CORRECTION = mapOf(
    ConstructorParameterData("TimeSpan", "millis") to "milliseconds",

    ConstructorParameterData("SvgDefsManager", "defsElement") to "defs",

    ConstructorParameterData("LineSegment", "p1") to "firstEndPoint",
    ConstructorParameterData("LineSegment", "p2") to "secondEndPoint",

    ConstructorParameterData("NodeEventArgs", "oldParent") to "parent",
    ConstructorParameterData("NodeEventArgs", "oldIsGroupNode") to "isGroupNode",
    ConstructorParameterData("FoldingEdgeStateId", "sourceCollapsed") to "sourceIsCollapsed",
    ConstructorParameterData("FoldingEdgeStateId", "targetCollapsed") to "targetIsCollapsed",

    ConstructorParameterData("HandleSerializationEventArgs", "serializationType") to "sourceType",
    ConstructorParameterData("QueryTypeEventArgs", "xName") to "xmlName",
    ConstructorParameterData("XmlName", "ns") to "namespace",

    ConstructorParameterData("EdgeCellInfo", "enterSegmentNo") to "enterSegmentIndex",
    ConstructorParameterData("OrthogonalInterval", "isVertical") to "vertical",

    ConstructorParameterData("KeyEventArgs", "type") to "eventType",
    ConstructorParameterData("SvgVisual", "element") to "svgElement"
)

internal val PARAMETERS_CORRECTION = mapOf(
    ParameterData("IComparable", "compareTo", "obj") to "o",
    ParameterData("TimeSpan", "compareTo", "obj") to "o",
    ParameterData("IEnumerable", "includes", "value") to "item",
//    ParameterData("IListEnumerable", "get", "i") to "index",
    ParameterData("ListEnumerable", "get", "i") to "index",
    ParameterData("ResultItemCollection", "get", "i") to "index",

    ParameterData("YList", "insert", "element") to "item",
    ParameterData("YList", "remove", "element") to "item",

    ParameterData("LinearGradient", "accept", "item") to "node",
    ParameterData("RadialGradient", "accept", "item") to "node",

    ParameterData("GraphMLParseValueSerializerContext", "lookup", "serviceType") to "type",
    ParameterData("GraphMLWriteValueSerializerContext", "lookup", "serviceType") to "type",

    ParameterData("ReparentStripeHandler", "reparent", "stripe") to "movedStripe",
    ParameterData("StripeDropInputMode", "updatePreview", "newLocation") to "dragLocation",

    ParameterData("NodeReshapeHandleProvider", "getHandle", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "cancelDrag", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "dragFinished", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "handleMove", "inputModeContext") to "context",
    ParameterData("NodeReshapeHandlerHandle", "initializeDrag", "inputModeContext") to "context",
)

internal val PARAMETERS_NULLABILITY_CORRECTION = mapOf(
    ParameterData("LineSegment", "contains", "point") to false,

    ParameterData("IGraph", "contains", "item") to false,
    ParameterData("IGraph", "isGroupNode", "node") to false,
    ParameterData("IGraph", "setIsGroupNode", "node") to false,
    ParameterData("IGraph", "addLabel", "preferredSize") to true,

    ParameterData("Graph", "contains", "item") to false,
    ParameterData("Graph", "isGroupNode", "node") to false,

    ParameterData("FilteredGraphWrapper", "contains", "item") to false,
    ParameterData("FilteredGraphWrapper", "isGroupNode", "node") to false,
    ParameterData("FilteredGraphWrapper", "setIsGroupNode", "node") to false,
    ParameterData("GraphWrapperBase", "contains", "item") to false,
    ParameterData("GraphWrapperBase", "isGroupNode", "node") to false,
    ParameterData("GraphWrapperBase", "setIsGroupNode", "node") to false,

    ParameterData("GraphModelManager", "update", "item") to false,

    ParameterData("NavigationInputMode", "enterGroup", "node") to false,

    ParameterData("CreationProperties", "get", "key") to true,
    ParameterData("CreationProperties", "set", "key") to true,

    ParameterData("IAnimation", "createEasedAnimation", "easeIn") to true,
    ParameterData("IAnimation", "createEasedAnimation", "easeOut") to true,

    ParameterData("ItemModelManager", "itemAddedHandler", "source") to false,

    ParameterData("Table", "createChildColumn", "width") to false,
    ParameterData("Table", "createChildColumn", "minWidth") to false,
    ParameterData("Table", "createChildColumn", "padding") to false,
    ParameterData("Table", "createChildRow", "height") to false,
    ParameterData("Table", "createChildRow", "minHeight") to false,
    ParameterData("Table", "createChildRow", "padding") to false,
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
    MethodDeclaration("HierarchicalClusteringResult", "getDendrogramNode") to false,

    MethodDeclaration(className = "IEnumerable", methodName = "elementAt") to false,
    MethodDeclaration(className = "IEnumerable", methodName = "first") to false,
    MethodDeclaration(className = "IEnumerable", methodName = "last") to false,

    MethodDeclaration(className = "ExcludingFoldingEdgeConverter", methodName = "addFoldingEdge") to true,
    MethodDeclaration(className = "FilteredGraphWrapper", methodName = "getParent") to true,
    MethodDeclaration(className = "GraphWrapperBase", methodName = "getParent") to true,
    MethodDeclaration(className = "MergingFoldingEdgeConverter", methodName = "addFoldingEdge") to true,

    MethodDeclaration(className = "ChildParseContext", methodName = "getCurrent") to true,
    MethodDeclaration(className = "ChildWriteContext", methodName = "getCurrent") to true,
    MethodDeclaration(className = "ChildWriteContext", methodName = "getSerializationProperty") to true,
    MethodDeclaration(className = "MapperInputHandler", methodName = "parseDataCore") to true,
    MethodDeclaration(className = "MapperOutputHandler", methodName = "getValue") to true,

    MethodDeclaration(className = "GraphMLParseValueSerializerContext", methodName = "getValueSerializerFor") to true,
    MethodDeclaration(className = "GraphMLWriteValueSerializerContext", methodName = "getValueSerializerFor") to true,

    MethodDeclaration(className = "NodeDropInputMode", methodName = "getDropTarget") to true,

    MethodDeclaration(className = "ColorExtension", methodName = "provideValue") to true,

    MethodDeclaration(className = "FocusIndicatorManager", methodName = "add") to true,
    MethodDeclaration(className = "HighlightIndicatorManager", methodName = "getInstaller") to true,
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

    PropertyDeclaration(className = "StripeSelection", propertyName = "size")
)

internal val DUPLICATED_METHODS = listOf(
    MethodDeclaration(className = "YList", methodName = "elementAt"),
    MethodDeclaration(className = "YList", methodName = "includes"),
    MethodDeclaration(className = "YList", methodName = "toArray"),
    MethodDeclaration(className = "IEnumerable", methodName = "filter"),

    MethodDeclaration(className = "ICollection", methodName = "includes"),
    MethodDeclaration(className = "List", methodName = "includes"),
    MethodDeclaration(className = "List", methodName = "toArray"),
    MethodDeclaration(className = "HashMap", methodName = "includes"),
    MethodDeclaration(className = "ObservableCollection", methodName = "includes"),

    MethodDeclaration(className = "GraphMLIOHandler", methodName = "addTypeInformation"),
)

internal val INVALID_METHOD_OVERRIDES = listOf(
    MethodDeclaration(className = "Rect", methodName = "toSize"),
    MethodDeclaration(className = "MutableRectangle", methodName = "toRect"),
    MethodDeclaration(className = "YList", methodName = "includes"),
    MethodDeclaration(className = "List", methodName = "includes"),
    MethodDeclaration(className = "ICollection", methodName = "includes"),
    MethodDeclaration(className = "HashMap", methodName = "includes"),
    MethodDeclaration(className = "ObservableCollection", methodName = "includes"),
    MethodDeclaration(className = "StripeSelection", methodName = "includes"),
)

internal val INVALID_PROPERTY_OVERRIDES = listOf(
    PropertyDeclaration(className = "ResultItemMapping", propertyName = "size"),
    PropertyDeclaration(className = "ResultItemCollection", propertyName = "size"),
    PropertyDeclaration(className = "ListEnumerable", propertyName = "size"),
    PropertyDeclaration(className = "ICollection", propertyName = "size"),
    PropertyDeclaration(className = "HashMap", propertyName = "size"),
    PropertyDeclaration(className = "IListEnumerable", propertyName = "size"),
    PropertyDeclaration(className = "List", propertyName = "size"),
    PropertyDeclaration(className = "ObservableCollection", propertyName = "size"),
    PropertyDeclaration(className = "YList", propertyName = "size"),
    PropertyDeclaration(className = "StripeSelection", propertyName = "size"),
    PropertyDeclaration(className = "OrientedRectangle", propertyName = "bounds"),
    PropertyDeclaration(className = "OrientedRectangle", propertyName = "center"),
    PropertyDeclaration(className = "Graph", propertyName = "undoEngineEnabled"),
)

internal val SYSTEM_FUNCTIONS = listOf(
    "equals",
    "hashCode",
    "toString"
)

internal data class ConstructorParameterData(
    val className: String,
    val parameterName: String,
)

internal data class ParameterData(
    val className: String,
    val methodName: String,
    val parameterName: String,
    val last: Boolean = false,
)

internal data class PropertyDeclaration(
    val className: String,
    val propertyName: String,
)

internal data class MethodDeclaration(
    val className: String,
    val methodName: String,
)

internal data class MethodData(
    val className: String,
    val methodName: String,
    val parameters: List<MethodParameterData> = emptyList(),
    val result: ResultData? = null,
)

internal data class MethodParameterData(
    val name: String,
    val type: String,
    private val nullable: Boolean = false,
) {
    val modifiers = if (nullable) setOf(CANBENULL) else emptySet()
}

internal data class ResultData(
    val type: String,
    private val nullable: Boolean = false,
) {
    val modifiers = if (nullable) setOf(CANBENULL) else emptySet()
}
