package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.first
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jObject
import org.json.JSONObject
import kotlin.collections.first

private fun JSONObject.addMethod(
    methodData: MethodData
) {
    if (!has("methods")) {
        put("methods", emptyList<Any>())
    }

    val result = methodData.result
    var modifiers = listOf(PUBLIC)
    if (result != null) {
        modifiers += result.modifiers
    }

    getJSONArray("methods")
        .put(
            mutableMapOf(
                "name" to methodData.methodName,
                "modifiers" to modifiers
            )
                .also {
                    val parameters = methodData.parameters
                    if (parameters.isNotEmpty()) {
                        it.put(
                            "parameters",
                            parameters.map {
                                mapOf(
                                    "name" to it.name,
                                    "type" to it.type,
                                    "modifiers" to it.modifiers
                                )
                            }
                        )
                    }
                }
                .also {
                    if (result != null) {
                        it.put(
                            "returns", mapOf(
                                "type" to result.type
                            )
                        )
                    }
                }
        )
}

internal fun applyHacks(api: JSONObject) {
    val source = Source(api)

    removeDuplicatedProperties(source)
    removeDuplicatedMethods(source)
    removeSystemMethods(source)
    removeArtifitialParameters(source)

    fixUnionMethods(source)
    fixConstantGenerics(source)
    fixFunctionGenerics(source)

    fixReturnType(source)
    fixExtendedType(source)
    fixImplementedTypes(source)

    fixConstructorParameterNullability(source)

    fixPropertyType(source)
    fixPropertyNullability(source)

    fixMethodParameterName(source)
    fixMethodParameterType(source)
    fixMethodParameterNullability(source)
    fixMethodNullability(source)

    addMissedProperties(source)
    addMissedMethods(source)
    fieldToProperties(source)

    addClassGeneric(source)
}

private fun addClassGeneric(source: Source) {
    source.type("Class")
        .addStandardGeneric()

    source.allMethods(
        "lookup",
        "innerLookup",
        "contextLookup",
        "lookupContext",
        "inputModeContextLookup",
        "childInputModeContextLookup",
        "getCopy",
        "getOrCreateCopy"
    )
        .forEach {
            it.addStandardGeneric()

            it.typeParameter.addGeneric("T")

            it.getJSONObject("returns")
                .put("type", "T")

            it.getJSONArray("modifiers")
                .put(CANBENULL)
        }

    source.allMethods("getDecoratorFor")
        .forEach {
            it.firstParameter.addGeneric("TInterface")
        }

    source.allMethods(
        "typedHitElementsAt",
        "createHitTester",

        "serializeCore",
        "deserializeCore"
    )
        .forEach {
            it.firstParameter.addGeneric("T")
        }

    source.allMethods(
        "getCurrent",
        "serialize",
        "deserialize",
        "setLookup"
    )
        .map { it.firstParameter }
        .filter { it.getString("type") == YCLASS }
        .forEach {
            it.addGeneric("T")
        }

    source.allMethods("factoryLookupChainLink", "add", "addConstant")
        .filter { it.firstParameter.getString("name") == "contextType" }
        .forEach {
            it.parameter("contextType").addGeneric("TContext")
            it.parameter("resultType").addGeneric("TResult")
        }

    source.allMethods(
        "addGraphInputData",
        "addGraphOutputData"
    )
        .forEach {
            it.firstParameter.addGeneric("TValue")
        }

    source.allMethods("addOutputMapper")
        .forEach {
            it.parameter("modelItemType").addGeneric("TModelItem")
            it.parameter("dataType").addGeneric("TValue")
        }

    source.allMethods("addRegistryOutputMapper")
        .filter { it.firstParameter.getString("name") == "modelItemType" }
        .forEach {
            it.parameter("modelItemType").addGeneric("TModelItem")
            it.parameter("valueType").addGeneric("TValue")
        }

    source.type("GraphMLIOHandler")
        .apply {
            (jsequence("methods") + jsequence("staticMethods"))
                .optionalArray("parameters")
                .filter { it.getString("type") == YCLASS }
                .forEach {
                    when (it.getString("name")) {
                        "keyType" -> it.addGeneric("TKey")
                        "modelItemType" -> it.addGeneric("TKey")
                        "dataType" -> it.addGeneric("TData")
                    }
                }
        }


    source.allMethods(
        "addMapper",
        "addConstantMapper",
        "addDelegateMapper",

        // "createMapper",
        "createConstantMapper",
        "createDelegateMapper",

        "addDataProvider",
        "createDataMap",
        "createDataProvider"
    )
        .filter { it.firstParameter.getString("name") == "keyType" }
        .forEach {
            it.parameter("keyType").addGeneric("K")
            it.parameter("valueType").addGeneric("V")
        }

    source.types()
        .forEach { type ->
            val typeName = type.getString("name")
            if (typeName == "MapperMetadata") {
                return@forEach
            }

            type.optionalArray("constructors")
                .optionalArray("parameters")
                .filter { it.getString("type") == YCLASS }
                .forEach {
                    val name = it.getString("name")
                    val generic = when (name) {
                        "edgeStyleType" -> "TStyle"
                        "decoratedType" -> "TDecoratedType"
                        "interfaceType" -> "TInterface"
                        "keyType" ->
                            when (typeName) {
                                "DataMapAdapter" -> "K"
                                "ItemCollectionMapping" -> "TItem"
                                else -> "TKey"
                            }
                        "valueType" -> if (typeName == "DataMapAdapter") "V" else "TValue"
                        "dataType" -> "TData"
                        "itemType" -> "T"
                        "type" -> when (typeName) {
                            "StripeDecorator" -> "TStripe"
                            else -> null
                        }
                        else -> null
                    }

                    if (generic != null) {
                        it.addGeneric(generic)
                    }
                }
        }
}

private fun fixUnionMethods(source: Source) {
    val methods = source.type("GraphModelManager")
        .getJSONArray("methods")

    val unionMethods = methods
        .asSequence()
        .map { it as JSONObject }
        .filter { it.getString("name") == "getCanvasObjectGroup" }
        .toList()

    unionMethods
        .asSequence()
        .drop(1)
        .forEach { methods.remove(methods.indexOf(it)) }

    unionMethods.first()
        .firstParameter
        .apply {
            put("name", "item")
            put("type", "yfiles.graph.IModelItem")
        }

    // TODO: remove documentation
}

private fun fixConstantGenerics(source: Source) {
    source.type("IListEnumerable")
        .getJSONArray("constants")
        .firstWithName("EMPTY")
        .also {
            val type = it.getString("type")
                .replace("<T>", "<$JS_OBJECT>")
            it.put("type", type)
        }
}

private fun fixFunctionGenerics(source: Source) {
    source.type("List")
        .getJSONArray("staticMethods")
        .firstWithName("fromArray")
        .addStandardGeneric()

    source.type("List")
        .getJSONArray("staticMethods")
        .firstWithName("from")
        .getJSONArray("typeparameters")
        .put(jObject("name" to "T"))

    source.type("IContextLookupChainLink")
        .getJSONArray("staticMethods")
        .firstWithName("addingLookupChainLink")
        .apply {
            addStandardGeneric("TResult")
            firstParameter.addGeneric("TResult")
        }
}

private fun fixReturnType(source: Source) {
    sequenceOf("EdgeList", "YNodeList")
        .map { source.type(it) }
        .forEach {
            it.getJSONArray("methods")
                .firstWithName("getEnumerator")
                .getJSONObject("returns")
                .put("type", "yfiles.collections.IEnumerator<$JS_OBJECT>")
        }
}

private fun fixExtendedType(source: Source) {
    source.type("Exception")
        .remove("extends")
}

private fun fixImplementedTypes(source: Source) {
    sequenceOf("EdgeList", "YNodeList")
        .map { source.type(it) }
        .forEach { it.remove("implements") }
}

private val STRICT_CONSTRUCTOR_CLASSES = setOf(
    "DpKeyBase",
    "EdgeDpKey",
    "GraphDpKey",
    "GraphObjectDpKey",
    "IEdgeLabelLayoutDpKey",
    "IEdgeLabelLayoutDpKey",
    "ILabelLayoutDpKey",
    "INodeLabelLayoutDpKey",
    "NodeDpKey",

    "DataMapAdapter"
)

private fun fixConstructorParameterNullability(source: Source) {
    STRICT_CONSTRUCTOR_CLASSES
        .asSequence()
        .map { source.type(it) }
        .forEach {
            it.jsequence("constructors")
                .jsequence("parameters")
                .forEach { it.changeNullability(false, false) }
        }
}

private fun fixPropertyType(source: Source) {
    sequenceOf("SeriesParallelLayoutData", "TreeLayoutData")
        .map { source.type(it) }
        .forEach {
            it.getJSONArray("properties")
                .firstWithName("outEdgeComparers")
                .put("type", "yfiles.layout.ItemMapping<yfiles.graph.INode,Comparator<yfiles.graph.IEdge>>")
        }
}

private val PROPERTY_NULLABILITY_CORRECTION = mapOf(
    PropertyDeclaration("DefaultGraph", "tag") to true,
    PropertyDeclaration("GraphWrapperBase", "tag") to true,
    PropertyDeclaration("SimpleBend", "tag") to true,
    PropertyDeclaration("SimpleEdge", "tag") to true,
    PropertyDeclaration("SimpleLabel", "tag") to true,
    PropertyDeclaration("SimpleNode", "tag") to true,
    PropertyDeclaration("SimplePort", "tag") to true,

    PropertyDeclaration("IEdge", "sourcePort") to false,
    PropertyDeclaration("IEdge", "targetPort") to false
)

private fun fixPropertyNullability(source: Source) {
    PROPERTY_NULLABILITY_CORRECTION.forEach { (className, propertyName), nullable ->
        source
            .type(className)
            .getJSONArray("properties")
            .first { it.get("name") == propertyName }
            .changeNullability(nullable)
    }
}

private val PARAMETERS_CORRECTION = mapOf(
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

private fun fixMethodParameterName(source: Source) {
    PARAMETERS_CORRECTION.forEach { data, fixedName ->
        source.type(data.className)
            .methodParameters(data.methodName, data.parameterName, { it.getString("name") != fixedName })
            .first()
            .put("name", fixedName)
    }
}

private val PARAMETERS_NULLABILITY_CORRECTION = mapOf(
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

private val BROKEN_NULLABILITY_METHODS = setOf("applyLayout", "applyLayoutCore")

private fun fixMethodParameterNullability(source: Source) {
    PARAMETERS_NULLABILITY_CORRECTION
        .forEach { data, nullable ->
            val parameters = source.type(data.className)
                .methodParameters(data.methodName, data.parameterName, { true })

            val parameter = if (data.last) {
                parameters.last()
            } else {
                parameters.first()
            }

            parameter.changeNullability(nullable)
        }

    source.types()
        .optionalArray("methods")
        .filter { it.get("name") in BROKEN_NULLABILITY_METHODS }
        .filter { it.getJSONArray("parameters").length() == 1 }
        .map { it.getJSONArray("parameters").single() }
        .map { it as JSONObject }
        .onEach { require(it.getString("type") == "yfiles.layout.LayoutGraph") }
        .forEach { it.changeNullability(false) }
}

private fun fixMethodParameterType(source: Source) {
    source.type("IContextLookupChainLink")
        .getJSONArray("staticMethods")
        .firstWithName("addingLookupChainLink")
        .parameter("instance")
        .put("type", "TResult")
}

private val METHOD_NULLABILITY_MAP = mapOf(
    MethodDeclaration(className = "Graph", methodName = "getDataProvider") to true,
    MethodDeclaration(className = "ViewportLimiter", methodName = "getCurrentBounds") to true,
    MethodDeclaration(className = "IEnumerable", methodName = "first") to false
)

private fun fixMethodNullability(source: Source) {
    METHOD_NULLABILITY_MAP
        .forEach { (className, methodName), nullable ->
            source.type(className)
                .getJSONArray("methods")
                .firstWithName(methodName)
                .changeNullability(nullable)
        }
}

private val MISSED_PROPERTIES = listOf(
    PropertyData(className = "YList", propertyName = "isReadOnly", type = JS_BOOLEAN),
    PropertyData(className = "Arrow", propertyName = "length", type = JS_NUMBER)
)

private val MISSED_METHODS = listOf(
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

private fun addMissedProperties(source: Source) {
    MISSED_PROPERTIES
        .forEach { data ->
            source.type(data.className)
                .addProperty(data.propertyName, data.type)
        }
}

private fun addMissedMethods(source: Source) {
    MISSED_METHODS.forEach { data ->
        source.type(data.className)
            .addMethod(data)
    }
}

private val DUPLICATED_PROPERTIES = listOf(
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

private fun removeDuplicatedProperties(source: Source) {
    DUPLICATED_PROPERTIES
        .forEach { declaration ->
            val properties = source
                .type(declaration.className)
                .getJSONArray("properties")

            val property = properties
                .firstWithName(declaration.propertyName)

            properties.remove(properties.indexOf(property))
        }
}

private val DUPLICATED_METHODS = listOf(
    MethodDeclaration(className = "YList", methodName = "elementAt"),
    MethodDeclaration(className = "YList", methodName = "includes"),
    MethodDeclaration(className = "YList", methodName = "toArray"),

    MethodDeclaration(className = "ICollection", methodName = "includes"),
    MethodDeclaration(className = "List", methodName = "includes"),
    MethodDeclaration(className = "List", methodName = "toArray"),
    MethodDeclaration(className = "HashMap", methodName = "includes"),
    MethodDeclaration(className = "ObservableCollection", methodName = "includes")
)

private fun removeDuplicatedMethods(source: Source) {
    DUPLICATED_METHODS
        .forEach { declaration ->
            val methods = source
                .type(declaration.className)
                .getJSONArray("methods")

            val method = methods
                .firstWithName(declaration.methodName)

            methods.remove(methods.indexOf(method))
        }
}

private val SYSTEM_FUNCTIONS = listOf(
    "equals",
    "hashCode",
    "toString"
)

private fun removeSystemMethods(source: Source) {
    source.types()
        .filter { it.has("methods") }
        .forEach {
            val methods = it.getJSONArray("methods")
            val systemMetods = methods.asSequence()
                .map { it as JSONObject }
                .filter { it.getString("name") in SYSTEM_FUNCTIONS }
                .toList()

            systemMetods.forEach {
                methods.remove(methods.indexOf(it))
            }
        }
}

private fun removeArtifitialParameters(source: Source) {
    sequenceOf("constructors", "methods")
        .flatMap { parameter ->
            source.types()
                .filter { it.has(parameter) }
                .jsequence(parameter)
        }
        .filter { it.has("parameters") }
        .forEach {
            val artifitialParameters = it.jsequence("parameters")
                .filter { it.getJSONArray("modifiers").contains(ARTIFICIAL) }
                .toList()

            val parameters = it.getJSONArray("parameters")
            artifitialParameters.forEach {
                parameters.remove(parameters.indexOf(it))
            }
        }
}

private fun fieldToProperties(source: Source) {
    source.types()
        .filter { it.has("fields") }
        .forEach { type ->
            val fields = type.getJSONArray("fields")
            if (type.has("properties")) {
                val properties = type.getJSONArray("properties")
                fields.forEach { properties.put(it) }
            } else {
                type.put("properties", fields)
            }
            type.remove("fields")
        }
}

private data class ParameterData(
    val className: String,
    val methodName: String,
    val parameterName: String,
    val last: Boolean = false
)

private data class PropertyDeclaration(
    val className: String,
    val propertyName: String
)

private data class PropertyData(
    val className: String,
    val propertyName: String,
    val type: String
)

private data class MethodDeclaration(
    val className: String,
    val methodName: String
)

private data class MethodData(
    val className: String,
    val methodName: String,
    val parameters: List<MethodParameterData> = emptyList(),
    val result: ResultData? = null
)

private data class MethodParameterData(
    val name: String,
    val type: String,
    private val nullable: Boolean = false
) {
    val modifiers = if (nullable) setOf(CANBENULL) else emptySet()
}

private data class ResultData(
    val type: String,
    private val nullable: Boolean = false
) {
    val modifiers = if (nullable) setOf(CANBENULL) else emptySet()
}