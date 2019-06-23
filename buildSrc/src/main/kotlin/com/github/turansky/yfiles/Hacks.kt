package com.github.turansky.yfiles

import org.json.JSONObject


private val SYSTEM_FUNCTIONS = listOf("hashCode", "toString")

internal fun redundantMethod(method: Method): Boolean {
    if (method.name == "equals") {
        return true
    }

    return method.name in SYSTEM_FUNCTIONS && method.parameters.isEmpty()
}

private fun JSONObject.type(id: String): JSONObject {
    val rootPackage = id.substring(0, id.indexOf("."))
    val typePackage = id.substring(0, id.lastIndexOf("."))
    return this.getJSONArray("namespaces")
        .firstWithId(rootPackage)
        .getJSONArray("namespaces")
        .firstWithId(typePackage)
        .getJSONArray("types")
        .firstWithId(id)
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

    require(result.isNotEmpty())
    { "No method parameters found for object: $this, method: $methodName, parameter: $parameterName" }

    return result
}

private fun JSONObject.addProperty(
    propertyName: String,
    type: String
) {
    getJSONArray("properties")
        .put(
            mapOf(
                "name" to propertyName,
                "modifiers" to listOf(PUBLIC, FINAL, RO),
                "type" to type
            )
        )
}

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

private fun JSONObject.changeNullability(nullable: Boolean) {
    val modifiers = getJSONArray("modifiers")
    if (nullable) {
        modifiers.put(CANBENULL)
    } else {
        modifiers.remove(modifiers.indexOf(CANBENULL))
    }
}

internal object Hacks {
    fun applyHacks(source: JSONObject) {
        removeDuplicatedProperties(source)
        removeDuplicatedMethods(source)

        fixConstantGenerics(source)
        fixFunctionGenerics(source)

        fixReturnType(source)
        fixExtendedType(source)
        fixImplementedTypes(source)
        fixPropertyType(source)
        fixPropertyNullability(source)
        fixMethodParameterName(source)
        fixMethodParameterNullability(source)
        fixMethodNullability(source)

        addMissedProperties(source)
        addMissedMethods(source)
        fieldToProperties(source)

        addClassGeneric(source)
    }

    private fun addClassGeneric(source: JSONObject) {
        source.type("yfiles.lang.Class")
            .addStandardGeneric()

        source.allMethods("lookup", "childInputModeContextLookup")
            .forEach {
                it.addStandardGeneric()

                it.firstParameter.put("type", "yfiles.lang.Class<T>")

                it.getJSONObject("returns")
                    .put("type", "T")

                it.getJSONArray("modifiers")
                    .put(CANBENULL)
            }

        source.allMethods("getDecoratorFor")
            .forEach {
                it.firstParameter.put("type", "yfiles.lang.Class<TInterface>")
            }

        source.allMethods("typedHitElementsAt")
            .forEach {
                it.firstParameter.put("type", "yfiles.lang.Class<T>")
            }
    }

    private fun fixConstantGenerics(source: JSONObject) {
        source.type("yfiles.collections.IListEnumerable")
            .getJSONArray("constants")
            .firstWithName("EMPTY")
            .also {
                val type = it.getString("type")
                    .replace("<T>", "<Object>")
                it.put("type", type)
            }
    }

    private fun fixFunctionGenerics(source: JSONObject) {
        source.type("yfiles.collections.List")
            .getJSONArray("staticMethods")
            .firstWithName("fromArray")
            .addStandardGeneric()

        // WA: generic 'TContext' is temporally unused (while Class has no generics)
        source.type("yfiles.graph.ILookupDecorator")
            .getJSONArray("methods")
            .firstWithName("addConstant")
            .getJSONArray("typeparameters")
            .remove(0)

        source.type("yfiles.collections.List")
            .getJSONArray("staticMethods")
            .firstWithName("from")
            .getJSONArray("typeparameters")
            .put(jObject("name" to "T"))
    }

    private fun fixReturnType(source: JSONObject) {
        sequenceOf("yfiles.algorithms.EdgeList", "yfiles.algorithms.NodeList")
            .map { source.type(it) }
            .forEach {
                it.getJSONArray("methods")
                    .firstWithName("getEnumerator")
                    .getJSONObject("returns")
                    .put("type", "yfiles.collections.IEnumerator<${JS_OBJECT}>")
            }
    }

    private fun fixExtendedType(source: JSONObject) {
        source.type("yfiles.lang.Exception")
            .remove("extends")
    }

    private fun fixImplementedTypes(source: JSONObject) {
        sequenceOf("yfiles.algorithms.EdgeList", "yfiles.algorithms.NodeList")
            .map { source.type(it) }
            .forEach { it.remove("implements") }
    }

    private fun fixPropertyType(source: JSONObject) {
        sequenceOf("yfiles.seriesparallel.SeriesParallelLayoutData", "yfiles.tree.TreeLayoutData")
            .map { source.type(it) }
            .forEach {
                it.getJSONArray("properties")
                    .firstWithName("outEdgeComparers")
                    .put("type", "yfiles.layout.ItemMapping<yfiles.graph.INode,Comparator<yfiles.graph.IEdge>>")
            }
    }

    private val PROPERTY_NULLABILITY_CORRECTION = mapOf(
        PropertyDeclaration("yfiles.graph.DefaultGraph", "tag") to true,
        PropertyDeclaration("yfiles.graph.GraphWrapperBase", "tag") to true,
        PropertyDeclaration("yfiles.graph.SimpleBend", "tag") to true,
        PropertyDeclaration("yfiles.graph.SimpleEdge", "tag") to true,
        PropertyDeclaration("yfiles.graph.SimpleLabel", "tag") to true,
        PropertyDeclaration("yfiles.graph.SimpleNode", "tag") to true,
        PropertyDeclaration("yfiles.graph.SimplePort", "tag") to true,

        PropertyDeclaration("yfiles.graph.IEdge", "sourcePort") to false,
        PropertyDeclaration("yfiles.graph.IEdge", "targetPort") to false
    )

    private fun fixPropertyNullability(source: JSONObject) {
        PROPERTY_NULLABILITY_CORRECTION.forEach { (className, propertyName), nullable ->
            source
                .type(className)
                .getJSONArray("properties")
                .first { it.get("name") == propertyName }
                .changeNullability(nullable)
        }
    }

    private val PARAMETERS_CORRECTION = mapOf(
        ParameterData("yfiles.lang.IComparable", "compareTo", "obj") to "o",
        ParameterData("yfiles.lang.TimeSpan", "compareTo", "obj") to "o",
        ParameterData("yfiles.collections.IEnumerable", "includes", "value") to "item",

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
        ParameterData("yfiles.styles.IArrow", "getBoundsProvider", "directionVector") to "direction",
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

    private val PARAMETERS_NULLABILITY_CORRECTION = mapOf(
        ParameterData("yfiles.algorithms.YList", "copyTo", "array") to false,
        ParameterData("yfiles.collections.ObservableCollection", "copyTo", "array") to false,

        ParameterData("yfiles.graph.IGraph", "addPortAt", "style") to true,
        ParameterData("yfiles.graph.ILookupDecorator", "add", "nullIsFallback") to true,
        ParameterData("yfiles.graph.ILookupDecorator", "add", "decorateNull", true) to true,

        ParameterData("yfiles.graphml.CreationProperties", "get", "key") to true,
        ParameterData("yfiles.graphml.CreationProperties", "set", "key") to true,

        ParameterData("yfiles.styles.TemplatePortStyleRenderer", "updateVisual", "context") to false,
        ParameterData("yfiles.styles.TemplatePortStyleRenderer", "updateVisual", "oldVisual") to true,

        ParameterData("yfiles.view.IAnimation", "createEasedAnimation", "easeIn") to true,
        ParameterData("yfiles.view.IAnimation", "createEasedAnimation", "easeOut") to true,
        ParameterData("yfiles.view.FocusIndicatorManager", "getInstaller", "item") to true
    )

    private val BROKEN_NULLABILITY_METHODS = setOf("applyLayout", "applyLayoutCore")

    private fun fixMethodParameterNullability(source: JSONObject) {
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
            .filter { it.has("methods") }
            .flatMap { it.getJSONArray("methods").asSequence() }
            .map { it as JSONObject }
            .filter { it.get("name") in BROKEN_NULLABILITY_METHODS }
            .filter { it.getJSONArray("parameters").length() == 1 }
            .map { it.getJSONArray("parameters").single() }
            .map { it as JSONObject }
            .onEach { require(it.getString("type") == "yfiles.layout.LayoutGraph") }
            .forEach { it.changeNullability(false) }
    }

    private val METHOD_NULLABILITY_MAP = mapOf(
        MethodDeclaration(className = "yfiles.algorithms.Graph", methodName = "getDataProvider") to true,
        MethodDeclaration(className = "yfiles.view.ViewportLimiter", methodName = "getCurrentBounds") to true,
        MethodDeclaration(className = "yfiles.collections.IEnumerable", methodName = "first") to false
    )

    private fun fixMethodNullability(source: JSONObject) {
        METHOD_NULLABILITY_MAP
            .forEach { (className, methodName), nullable ->
                source.type(className)
                    .getJSONArray("methods")
                    .firstWithName(methodName)
                    .changeNullability(nullable)
            }
    }

    private val MISSED_PROPERTIES = listOf(
        PropertyData(className = "yfiles.algorithms.YList", propertyName = "isReadOnly", type = JS_BOOLEAN),
        PropertyData(className = "yfiles.styles.Arrow", propertyName = "length", type = JS_NUMBER)
    )

    private val MISSED_METHODS = listOf(
        MethodData(className = "yfiles.geometry.Matrix", methodName = "clone", result = ResultData(JS_OBJECT)),
        MethodData(className = "yfiles.geometry.MutablePoint", methodName = "clone", result = ResultData(JS_OBJECT)),
        MethodData(className = "yfiles.geometry.MutableSize", methodName = "clone", result = ResultData(JS_OBJECT)),
        MethodData(className = "yfiles.geometry.MutableRectangle", methodName = "clone", result = ResultData(JS_OBJECT)),
        MethodData(className = "yfiles.geometry.OrientedRectangle", methodName = "clone", result = ResultData(JS_OBJECT)),

        MethodData(
            className = "yfiles.algorithms.YList",
            methodName = "add",
            parameters = listOf(
                MethodParameterData("item", JS_OBJECT, true)
            )
        ),

        MethodData(
            className = "yfiles.graph.CompositeUndoUnit",
            methodName = "tryMergeUnit",
            parameters = listOf(
                MethodParameterData("unit", "IUndoUnit")
            ),
            result = ResultData(JS_BOOLEAN)
        ),
        MethodData(
            className = "yfiles.graph.CompositeUndoUnit",
            methodName = "tryReplaceUnit",
            parameters = listOf(
                MethodParameterData("unit", "IUndoUnit")
            ),
            result = ResultData(JS_BOOLEAN)
        ),

        MethodData(
            className = "yfiles.graph.EdgePathLabelModel",
            methodName = "findBestParameter",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("model", "ILabelModel"),
                MethodParameterData("layout", "yfiles.geometry.IOrientedRectangle")
            ),
            result = ResultData("ILabelModelParameter")
        ),
        MethodData(
            className = "yfiles.graph.EdgePathLabelModel",
            methodName = "getParameters",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("model", "ILabelModel")
            ),
            result = ResultData("yfiles.collections.IEnumerable<ILabelModelParameter>")
        ),
        MethodData(
            className = "yfiles.graph.EdgePathLabelModel",
            methodName = "getGeometry",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("layoutParameter", "ILabelModelParameter")
            ),
            result = ResultData("yfiles.geometry.IOrientedRectangle")
        ),

        MethodData(
            className = "yfiles.graph.EdgeSegmentLabelModel",
            methodName = "findBestParameter",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("model", "ILabelModel"),
                MethodParameterData("layout", "yfiles.geometry.IOrientedRectangle")
            ),
            result = ResultData("ILabelModelParameter")
        ),
        MethodData(
            className = "yfiles.graph.EdgeSegmentLabelModel",
            methodName = "getParameters",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("model", "ILabelModel")
            ),
            result = ResultData("yfiles.collections.IEnumerable<ILabelModelParameter>")
        ),
        MethodData(
            className = "yfiles.graph.EdgeSegmentLabelModel",
            methodName = "getGeometry",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("layoutParameter", "ILabelModelParameter")
            ),
            result = ResultData("yfiles.geometry.IOrientedRectangle")
        ),

        MethodData(
            className = "yfiles.graph.FreeLabelModel",
            methodName = "findBestParameter",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("model", "ILabelModel"),
                MethodParameterData("layout", "yfiles.geometry.IOrientedRectangle")
            ),
            result = ResultData("ILabelModelParameter")
        ),

        MethodData(
            className = "yfiles.graph.GenericLabelModel",
            methodName = "canConvert",
            parameters = listOf(
                MethodParameterData("context", "yfiles.graphml.IWriteContext"),
                MethodParameterData("value", JS_OBJECT)
            ),
            result = ResultData(JS_BOOLEAN)
        ),
        MethodData(
            className = "yfiles.graph.GenericLabelModel",
            methodName = "getParameters",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("model", "ILabelModel")
            ),
            result = ResultData("yfiles.collections.IEnumerable<ILabelModelParameter>")
        ),
        MethodData(
            className = "yfiles.graph.GenericLabelModel",
            methodName = "convert",
            parameters = listOf(
                MethodParameterData("context", "yfiles.graphml.IWriteContext"),
                MethodParameterData("value", JS_OBJECT)
            ),
            result = ResultData("yfiles.graphml.MarkupExtension")
        ),
        MethodData(
            className = "yfiles.graph.GenericLabelModel",
            methodName = "getGeometry",
            parameters = listOf(
                MethodParameterData("label", "ILabel"),
                MethodParameterData("layoutParameter", "ILabelModelParameter")
            ),
            result = ResultData("yfiles.geometry.IOrientedRectangle")
        ),

        MethodData(
            className = "yfiles.graph.GenericPortLocationModel",
            methodName = "canConvert",
            parameters = listOf(
                MethodParameterData("context", "yfiles.graphml.IWriteContext"),
                MethodParameterData("value", JS_OBJECT)
            ),
            result = ResultData(JS_BOOLEAN)
        ),
        MethodData(
            className = "yfiles.graph.GenericPortLocationModel",
            methodName = "convert",
            parameters = listOf(
                MethodParameterData("context", "yfiles.graphml.IWriteContext"),
                MethodParameterData("value", JS_OBJECT)
            ),
            result = ResultData("yfiles.graphml.MarkupExtension")
        ),
        MethodData(
            className = "yfiles.graph.GenericPortLocationModel",
            methodName = "getEnumerator",
            result = ResultData("yfiles.collections.IEnumerator<IPortLocationModelParameter>")
        ),

        MethodData(
            className = "yfiles.input.PortRelocationHandleProvider",
            methodName = "getHandle",
            parameters = listOf(
                MethodParameterData("context", "IInputModeContext"),
                MethodParameterData("edge", "yfiles.graph.IEdge"),
                MethodParameterData("sourceHandle", JS_BOOLEAN)
            ),
            result = ResultData("IHandle", true)
        ),

        MethodData(
            className = "yfiles.styles.Arrow",
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
            className = "yfiles.styles.Arrow",
            methodName = "getVisualCreator",
            parameters = listOf(
                MethodParameterData("edge", "yfiles.graph.IEdge"),
                MethodParameterData("atSource", JS_BOOLEAN),
                MethodParameterData("anchor", "yfiles.geometry.Point"),
                MethodParameterData("direction", "yfiles.geometry.Point")
            ),
            result = ResultData("yfiles.view.IVisualCreator")
        ),
        MethodData(className = "yfiles.styles.Arrow", methodName = "clone", result = ResultData(JS_OBJECT)),

        MethodData(
            className = "yfiles.styles.GraphOverviewSvgVisualCreator",
            methodName = "createVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext")
            ),
            result = ResultData("yfiles.view.Visual", true)
        ),
        MethodData(
            className = "yfiles.styles.GraphOverviewSvgVisualCreator",
            methodName = "updateVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext"),
                MethodParameterData("oldVisual", "yfiles.view.Visual", true)
            ),
            result = ResultData("yfiles.view.Visual", true)
        ),

        MethodData(
            className = "yfiles.view.GraphOverviewCanvasVisualCreator",
            methodName = "createVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext")
            ),
            result = ResultData("yfiles.view.Visual", true)
        ),
        MethodData(
            className = "yfiles.view.GraphOverviewCanvasVisualCreator",
            methodName = "updateVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext"),
                MethodParameterData("oldVisual", "yfiles.view.Visual", true)
            ),
            result = ResultData("yfiles.view.Visual", true)
        ),

        MethodData(
            className = "yfiles.view.DefaultPortCandidateDescriptor",
            methodName = "createVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext")
            ),
            result = ResultData("yfiles.view.Visual", true)
        ),
        MethodData(
            className = "yfiles.view.DefaultPortCandidateDescriptor",
            methodName = "updateVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext"),
                MethodParameterData("oldVisual", "yfiles.view.Visual", true)
            ),
            result = ResultData("yfiles.view.Visual", true)
        ),
        MethodData(
            className = "yfiles.view.DefaultPortCandidateDescriptor",
            methodName = "isInBox",
            parameters = listOf(
                MethodParameterData("context", "yfiles.input.IInputModeContext"),
                MethodParameterData("rectangle", "yfiles.geometry.Rect")
            ),
            result = ResultData(JS_BOOLEAN)
        ),
        MethodData(
            className = "yfiles.view.DefaultPortCandidateDescriptor",
            methodName = "isVisible",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.ICanvasContext"),
                MethodParameterData("rectangle", "yfiles.geometry.Rect")
            ),
            result = ResultData(JS_BOOLEAN)
        ),
        MethodData(
            className = "yfiles.view.DefaultPortCandidateDescriptor",
            methodName = "getBounds",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.ICanvasContext")
            ),
            result = ResultData("yfiles.geometry.Rect")
        ),
        MethodData(
            className = "yfiles.view.DefaultPortCandidateDescriptor",
            methodName = "isHit",
            parameters = listOf(
                MethodParameterData("context", "yfiles.input.IInputModeContext"),
                MethodParameterData("location", "yfiles.geometry.Point")
            ),
            result = ResultData(JS_BOOLEAN)
        ),
        MethodData(
            className = "yfiles.view.DefaultPortCandidateDescriptor",
            methodName = "isInPath",
            parameters = listOf(
                MethodParameterData("context", "yfiles.input.IInputModeContext"),
                MethodParameterData("lassoPath", "yfiles.geometry.GeneralPath")
            ),
            result = ResultData(JS_BOOLEAN)
        ),

        MethodData(className = "yfiles.styles.VoidPathGeometry", methodName = "getPath", result = ResultData("yfiles.geometry.GeneralPath", true)),
        MethodData(className = "yfiles.styles.VoidPathGeometry", methodName = "getSegmentCount", result = ResultData(JS_NUMBER)),
        MethodData(
            className = "yfiles.styles.VoidPathGeometry",
            methodName = "getTangent",
            parameters = listOf(
                MethodParameterData("ratio", JS_NUMBER)
            ),
            result = ResultData("yfiles.geometry.Tangent", true)
        ),
        MethodData(
            className = "yfiles.styles.VoidPathGeometry",
            methodName = "getTangent",
            parameters = listOf(
                MethodParameterData("segmentIndex", JS_NUMBER),
                MethodParameterData("ratio", JS_NUMBER)
            ),
            result = ResultData("yfiles.geometry.Tangent", true)
        ),
        MethodData(
            className = "yfiles.styles.GraphOverviewWebGLVisualCreator",
            methodName = "createVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext")
            ),
            result = ResultData("yfiles.view.Visual", true)
        ),
        MethodData(
            className = "yfiles.styles.GraphOverviewWebGLVisualCreator",
            methodName = "updateVisual",
            parameters = listOf(
                MethodParameterData("context", "yfiles.view.IRenderContext"),
                MethodParameterData("oldVisual", "yfiles.view.Visual", true)
            ),
            result = ResultData("yfiles.view.Visual", true)
        )
    )

    private fun addMissedProperties(source: JSONObject) {
        MISSED_PROPERTIES
            .forEach { data ->
                source.type(data.className)
                    .addProperty(data.propertyName, data.type)
            }
    }

    private fun addMissedMethods(source: JSONObject) {
        MISSED_METHODS.forEach { data ->
            source.type(data.className)
                .addMethod(data)
        }
    }

    private val DUPLICATED_PROPERTIES = listOf(
        PropertyDeclaration(className = "yfiles.algorithms.YList", propertyName = "size"),

        PropertyDeclaration(className = "yfiles.analysis.ResultItemCollection", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.analysis.ResultItemMapping", propertyName = "size"),

        PropertyDeclaration(className = "yfiles.collections.ICollection", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.collections.IListEnumerable", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.collections.List", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.collections.ListEnumerable", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.collections.Map", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.collections.ObservableCollection", propertyName = "size"),

        PropertyDeclaration(className = "yfiles.geometry.MutableRectangle", propertyName = "isEmpty"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "bottomLeft"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "bottomRight"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "center"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "isEmpty"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "maxX"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "maxY"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "topLeft"),
        PropertyDeclaration(className = "yfiles.geometry.Rect", propertyName = "topRight"),

        PropertyDeclaration(className = "yfiles.graph.DefaultGraph", propertyName = "undoEngineEnabled"),

        PropertyDeclaration(className = "yfiles.view.DefaultSelectionModel", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.view.GraphSelection", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.view.ISelectionModel", propertyName = "size"),
        PropertyDeclaration(className = "yfiles.view.StripeSelection", propertyName = "size")
    )

    private fun removeDuplicatedProperties(source: JSONObject) {
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
        MethodDeclaration(className = "yfiles.algorithms.YList", methodName = "elementAt"),
        MethodDeclaration(className = "yfiles.algorithms.YList", methodName = "includes"),
        MethodDeclaration(className = "yfiles.algorithms.YList", methodName = "toArray"),

        MethodDeclaration(className = "yfiles.collections.ICollection", methodName = "includes"),
        MethodDeclaration(className = "yfiles.collections.List", methodName = "includes"),
        MethodDeclaration(className = "yfiles.collections.List", methodName = "toArray"),
        MethodDeclaration(className = "yfiles.collections.Map", methodName = "includes"),
        MethodDeclaration(className = "yfiles.collections.ObservableCollection", methodName = "includes")
    )

    private fun removeDuplicatedMethods(source: JSONObject) {
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

    private fun fieldToProperties(source: JSONObject) {
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