package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IENUMERABLE
import com.github.turansky.yfiles.JS_DOUBLE
import com.github.turansky.yfiles.JS_INT
import com.github.turansky.yfiles.JS_NUMBER
import org.json.JSONObject

internal fun correctNumbers(source: JSONObject) {
    val types = Source(source)
        .types()
        .toList()

    correctEnumerable(types)

    types.asSequence()
        .onEach { it.correctConstants() }
        .onEach { it.correctConstructors() }
        .onEach { it.correctProperties() }
        .onEach { it.correctPropertiesGeneric() }
        .onEach { it.correctMethods() }
        .forEach { it.correctMethodParameters() }

    (source
        .get(FUNCTION_SIGNATURES)
        .getJSONObject("yfiles.view.AnimationCallback")
        .get(PARAMETERS)
        .single() as JSONObject)
        .set(TYPE, JS_DOUBLE)

}

private fun JSONObject.correctConstants() {
    if (!has(CONSTANTS)) {
        return
    }

    val className = get(NAME)
    flatMap(CONSTANTS)
        .filter { it[TYPE] != JS_NUMBER }
        .filter { JS_NUMBER in it[TYPE] }
        .forEach {
            if (it.has(SIGNATURE)) {
                check(className == "HierarchicalClustering")
                it.replaceInSignature(",$JS_NUMBER>", ",$JS_DOUBLE>")
                return@forEach
            }
        }
}

private fun JSONObject.correctConstructors() {
    if (!has(CONSTRUCTORS)) {
        return
    }

    val className = get(NAME)
    flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .filter { it[TYPE] == JS_NUMBER }
        .forEach { it[TYPE] = getConstructorParameterType(className, it[NAME]) }

    flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .filter { it[TYPE] == "Array<$JS_NUMBER>" }
        .forEach {
            val genericType = when (val name = it[NAME]) {
                "dashes" -> JS_DOUBLE
                "rowLayout", "columnLayout" -> JS_INT
                else -> throw IllegalStateException("Unexpected constructor parameter $name")
            }

            it[TYPE] = "Array<$genericType>"
        }
}

private val INT_CONSTRUCTOR_CLASSES = setOf(
    "TimeSpan"
)

private val INT_SIZE_CLASSES = setOf(
    "ICursor",
    "IEnumerable",
    "GeneralPath",
    "UndoEngine",
)

private val DOUBLE_CONSTRUCTOR_CLASSES = setOf(
    "BorderLine",
    "GridConstraintProvider",
    "YVector",
    "Matrix",
    "DefaultNodePlacer",
    "Interval",
    "MinimumNodeSizeStage",
    "FreeEdgeLabelLayoutModelParameter",
    "FreeNodeLabelLayoutModelParameter"
)

private fun getConstructorParameterType(className: String, parameterName: String): String {
    when (className) {
        in INT_CONSTRUCTOR_CLASSES -> return JS_INT
        in DOUBLE_CONSTRUCTOR_CLASSES -> return JS_DOUBLE
    }

    return when (parameterName) {
        in INT_CONSTRUCTOR_PARAMETERS -> JS_INT
        in DOUBLE_CONSTRUCTOR_PARAMETERS -> JS_DOUBLE
        else -> getPropertyType(className, parameterName)
    }
}

private fun JSONObject.correctProperties() {
    val className = get(NAME)
    optFlatMap(PROPERTIES)
        .filter { it[TYPE] == JS_NUMBER }
        .forEach { it[TYPE] = getPropertyType(className, it[NAME]) }
}

private val METHOD_MAPPING = mapOf(
        MethodDeclaration(className = "MultiPageLayoutContext", methodName = "getPage") to JS_INT,
        MethodDeclaration(className = "InteractiveOrganicLayoutData", methodName = "updateNodeCenters") to JS_INT,
        MethodDeclaration(className = "InteractiveOrganicNodeHandle", methodName = "getZ") to JS_DOUBLE,
        MethodDeclaration(className = "RadialTreeLayout", methodName = "getPreferredChildSectorAngle") to JS_DOUBLE,
        MethodDeclaration(className = "YList", methodName = "indexOfCell") to JS_DOUBLE,
        MethodDeclaration(className = "LineSegment", methodName = "length") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "diameter") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "density") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "simplexRankAssignment") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "clusteringCoefficient") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "biconnectedComponentClustering") to JS_INT,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "hierarchicalClustering") to JS_INT,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "kCores") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "labelPropagation") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "modularity") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "minimumCostFlow") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "maximumFlow") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "maximumFlowMinimumCut") to JS_DOUBLE,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "layerAssignment") to JS_INT,
        MethodDeclaration(className = "LayoutGraphAlgorithms", methodName = "getMinimum") to JS_DOUBLE,
        MethodDeclaration(className = "FromSketchLayerAssigner", methodName = "getMinimum") to JS_DOUBLE,
        MethodDeclaration(className = "FromSketchLayerAssigner", methodName = "getMaximum") to JS_DOUBLE,
        MethodDeclaration(className = "BorderLine", methodName = "getMinPosition") to JS_DOUBLE,
        MethodDeclaration(className = "BorderLine", methodName = "getMaxPosition") to JS_DOUBLE,
        MethodDeclaration(className = "BorderLine", methodName = "getMinValue") to JS_DOUBLE,
        MethodDeclaration(className = "BorderLine", methodName = "getMaxValue") to JS_DOUBLE,
)

private val PARAMETER_MAPPING = mapOf(
        (MethodDeclaration(className = "InteractiveOrganicLayoutData", methodName = "updateNodeCenters") to "minMovement") to JS_DOUBLE,
        (MethodDeclaration(className = "InteractiveOrganicNodeHandle", methodName = "setCenter3D") to "z") to JS_DOUBLE,
)

private val PROPERTY_MAPPING = mapOf(
        PropertyDeclaration(className = "IEnumerable", propertyName = "size") to JS_INT,
        PropertyDeclaration(className = "BalloonLayout", propertyName = "minimumNodeDistance") to JS_INT,
        PropertyDeclaration(className = "LineSegment", propertyName = "yIntercept") to JS_INT,
        PropertyDeclaration(className = "AffineLine", propertyName = "a") to JS_DOUBLE,
        PropertyDeclaration(className = "AffineLine", propertyName = "b") to JS_DOUBLE,
        PropertyDeclaration(className = "EdgeSegmentLabelModelParameter", propertyName = "segment") to JS_INT,
        PropertyDeclaration(className = "EdgeSegmentPortLocationModelParameter", propertyName = "segment") to JS_INT,
        PropertyDeclaration(className = "SmartEdgeLabelModelParameter", propertyName = "segment") to JS_INT,
        PropertyDeclaration(className = "CoordinateAssigner", propertyName = "layoutGridCrossingWeight") to JS_DOUBLE,
        PropertyDeclaration(className = "FromSketchLayerAssigner", propertyName = "nodeMargin") to JS_DOUBLE,
        PropertyDeclaration(className = "CreateEdgeInputMode", propertyName = "startPortCandidateHitRadius") to JS_DOUBLE,
        PropertyDeclaration(className = "SnapCircle", propertyName = "startAngle") to JS_DOUBLE,
        PropertyDeclaration(className = "SnapCircle", propertyName = "endAngle") to JS_DOUBLE,
        PropertyDeclaration(className = "SnapCircle", propertyName = "startAngle") to JS_DOUBLE,
        PropertyDeclaration(className = "SnapCircle", propertyName = "endAngle") to JS_DOUBLE,
        PropertyDeclaration(className = "BorderLine", propertyName = "minPosition") to JS_DOUBLE,
        PropertyDeclaration(className = "BorderLine", propertyName = "maxPosition") to JS_DOUBLE,
        PropertyDeclaration(className = "RadialGroupLayout", propertyName = "preferredRootSectorAngle") to JS_DOUBLE,
        PropertyDeclaration(className = "InteractiveOrganicLayout", propertyName = "defaultPreferredEdgeLength") to JS_DOUBLE,
        PropertyDeclaration(className = "InteractiveOrganicNodeHandle", propertyName = "stress") to JS_DOUBLE,
        PropertyDeclaration(className = "InteractiveOrganicNodeHandle", propertyName = "inertia") to JS_DOUBLE,
        PropertyDeclaration(className = "OrganicLayout", propertyName = "defaultPreferredEdgeLength") to JS_DOUBLE,
        PropertyDeclaration(className = "Interval", propertyName = "minimum") to JS_DOUBLE,
        PropertyDeclaration(className = "Interval", propertyName = "maximum") to JS_DOUBLE,
        PropertyDeclaration(className = "OrthogonalInterval", propertyName = "minimum") to JS_DOUBLE,
        PropertyDeclaration(className = "OrthogonalInterval", propertyName = "maximum") to JS_DOUBLE,
        PropertyDeclaration(className = "ParallelEdgeRouter", propertyName = "relativeJoinEndDistanceFactor") to JS_DOUBLE,
        PropertyDeclaration(className = "SeriesParallelLayout", propertyName = "parallelSubgraphAlignment") to JS_DOUBLE,
        PropertyDeclaration(className = "Arrow", propertyName = "lengthScale") to JS_DOUBLE,
        PropertyDeclaration(className = "Arrow", propertyName = "widthScale") to JS_DOUBLE,
        PropertyDeclaration(className = "Arrow", propertyName = "lengthScale") to JS_DOUBLE,
        PropertyDeclaration(className = "Arrow", propertyName = "widthScale") to JS_DOUBLE,
        PropertyDeclaration(className = "RadialTreeLayout", propertyName = "preferredChildSectorAngle") to JS_DOUBLE,
        PropertyDeclaration(className = "RadialTreeLayout", propertyName = "preferredRootSectorAngle") to JS_DOUBLE,
        PropertyDeclaration(className = "SingleLayerSubtreePlacer", propertyName = "minLastSegmentLength") to JS_DOUBLE,
        PropertyDeclaration(className = "SingleLayerSubtreePlacer", propertyName = "minSlope") to JS_DOUBLE,
        PropertyDeclaration(className = "SingleLayerSubtreePlacer", propertyName = "minSlopeHeight") to JS_DOUBLE,
        PropertyDeclaration(className = "PointerEventArgs", propertyName = "pointerId") to JS_INT,
        PropertyDeclaration(className = "PointerEventArgs", propertyName = "pressure") to JS_DOUBLE,
        PropertyDeclaration(className = "PointerEventArgs", propertyName = "wheelDeltaY") to JS_DOUBLE,
        PropertyDeclaration(className = "PointerEventArgs", propertyName = "wheelDeltaY") to JS_DOUBLE,
        PropertyDeclaration(className = "PointerEventArgs", propertyName = "pointerId") to JS_INT,
        PropertyDeclaration(className = "PointerEventArgs", propertyName = "pressure") to JS_DOUBLE,
)

private fun getPropertyType(className: String, propertyName: String): String =
    when {
        propertyName.endsWith("Count") -> JS_INT
        propertyName.endsWith("Index") -> JS_INT
        propertyName.endsWith("capacity") -> JS_INT
        propertyName.endsWith("Cost") -> JS_DOUBLE
        propertyName.endsWith("Ratio") -> JS_DOUBLE
        propertyName.endsWith("padding") -> JS_DOUBLE
        propertyName.endsWith("Padding") -> JS_DOUBLE
        propertyName.endsWith("Distance") -> JS_DOUBLE
        propertyName == "size" && className in INT_SIZE_CLASSES -> JS_INT
        propertyName in INT_PROPERTIES -> JS_INT
        propertyName in DOUBLE_PROPERTIES -> JS_DOUBLE
        else -> PROPERTY_MAPPING[PropertyDeclaration(className, propertyName)] ?: error("Unexpected property: $className.$propertyName")
    }

private fun JSONObject.correctPropertiesGeneric() {
    if (!has(PROPERTIES)) {
        return
    }

    flatMap(PROPERTIES)
        .filter { "$JS_NUMBER>" in it[TYPE] }
        .forEach { it[TYPE] = getPropertyGenericType(it[NAME], it[TYPE]) }

    flatMap(PROPERTIES)
        .filter { it.has(SIGNATURE) }
        .forEach {
            val signature = it[SIGNATURE]
            if (!signature.endsWith(",$JS_NUMBER>")) {
                return@forEach
            }

            val name = it[NAME]
            check(name == "metric" || name == "heuristic")
            it.put("signature", signature.replace("$JS_NUMBER>", "$JS_DOUBLE>"))
        }
}

private fun getPropertyGenericType(propertyName: String, type: String): String {
    val generic = if (
        propertyName.endsWith("Ids")
        || propertyName.endsWith("Indices")
        || propertyName.endsWith("Capacities", true)
        || propertyName == "busRootOffsets"
    ) {
        JS_INT
    } else {
        JS_DOUBLE
    }

    return type.replace(JS_NUMBER, generic)
}


private fun JSONObject.correctMethods() {
    val className = get(NAME)
    optFlatMap(METHODS)
        .filter { it.has(RETURNS) }
        .forEach {
            val methodName = it[NAME]
            val returns = it[RETURNS]

            when (returns[TYPE]) {
                JS_NUMBER -> returns[TYPE] = getReturnType(className, methodName)
                "Array<$JS_NUMBER>" -> returns[TYPE] = "Array<$JS_DOUBLE>"
                "$IENUMERABLE<$JS_NUMBER>" -> {
                    check(methodName == "ofRange" || methodName == "ofType") { "methodName=$methodName" }
                    returns[TYPE] = "$IENUMERABLE<$JS_INT>"
                }
            }
        }
}

private fun getReturnType(className: String, methodName: String): String =
    when {
        methodName.endsWith("Count") -> JS_INT
        methodName.endsWith("Components") -> JS_INT
        methodName.endsWith("Cost") || methodName.endsWith("Costs") -> JS_DOUBLE
        methodName.endsWith("Ratio") -> JS_DOUBLE
        methodName.endsWith("Distance") -> JS_DOUBLE
        methodName.endsWith("Intersection") -> JS_DOUBLE

        className == "YVector" || className == "LineSegment" && methodName == "length" -> JS_DOUBLE

        methodName in INT_METHODS -> JS_INT
        methodName in DOUBLE_METHODS -> JS_DOUBLE

        else -> METHOD_MAPPING[MethodDeclaration(className, methodName)] ?: error("Unexpected method: $className.$methodName")
    }

private fun JSONObject.correctMethodParameters() {
    val className = get(NAME)
    optFlatMap(METHODS)
        .filter { it.has(PARAMETERS) }
        .forEach { method ->
            val methodName = method[NAME]
            method.flatMap(PARAMETERS)
                .forEach {
                    val parameterName = it.get(NAME)
                    when (it.get(TYPE)) {
                        JS_NUMBER -> it[TYPE] = getParameterType(className, methodName, parameterName)
                        "Array<$JS_NUMBER>" -> {
                            val generic = getGenericParameterType(className, methodName, parameterName)
                            it[TYPE] = "Array<$generic>"
                        }
                        "Array<Array<$JS_NUMBER>>" -> {
                            check(parameterName == "dist")
                            it[TYPE] = "Array<Array<$JS_INT>>"
                        }
                    }
                }
        }
}

private val A_MAP = mapOf(
    "fromArgb" to JS_INT,
    "fromHSLA" to JS_DOUBLE,
    "fromRGBA" to JS_DOUBLE
)

private val DOUBLE_CLASSES = setOf(
    "BorderLine",
    "Interval",
    "TimeSpan",
    "NodeReshapeSnapResultProvider",
    "InteractiveOrganicLayout",
    "GraphSnapContext",
    "NodeHalo"
)

private val DOUBLE_METHOD_NAMES = setOf(
    "setNumber",
    "createHighPerformanceDoubleMap",
    "applyZoom",
    "createStripeAnimation"
)

private fun getParameterType(className: String, methodName: String, parameterName: String): String =
    when {
        methodName == "setInt" || methodName == "createHighPerformanceIntMap" -> JS_INT

        methodName in DOUBLE_METHOD_NAMES -> JS_DOUBLE
        className in DOUBLE_CLASSES -> JS_DOUBLE

        className == "List" || className == "IEnumerable" -> JS_INT

        parameterName.endsWith("Ratio") -> JS_DOUBLE
        parameterName.endsWith("Duration") -> JS_DOUBLE
        parameterName.endsWith("Distance") -> JS_DOUBLE
        parameterName.endsWith("Tolerance") -> JS_DOUBLE // added 3.0
        parameterName.endsWith("Width") -> JS_DOUBLE // added 3.0
        parameterName.endsWith("Height") -> JS_DOUBLE // added 3.0
        parameterName.endsWith("Intersection") -> JS_DOUBLE // added 3.0

        parameterName.endsWith("Index") -> JS_INT
        parameterName.endsWith("Count") -> JS_INT
        parameterName.endsWith("capacity") -> JS_INT
        parameterName.endsWith("size") -> JS_INT
        parameterName.endsWith("padding") -> JS_DOUBLE
        parameterName.endsWith("Padding") -> JS_DOUBLE

        parameterName == "a" -> A_MAP.getValue(methodName)

        parameterName in INT_METHOD_PARAMETERS -> JS_INT
        parameterName in INT_PROPERTIES -> JS_INT

        parameterName in DOUBLE_METHOD_PARAMETERS -> JS_DOUBLE
        parameterName in DOUBLE_PROPERTIES -> JS_DOUBLE

        className == "OrthogonalSnapLine" -> JS_DOUBLE
        className == "ReshapeRectangleContext" -> JS_DOUBLE
        className == "SnapConstraint" -> JS_DOUBLE
        className == "GenericLabeling" -> JS_DOUBLE
        className == "LayoutGrid" -> JS_DOUBLE

        else ->
            PARAMETER_MAPPING[MethodDeclaration(className, methodName) to parameterName] ?: error("Unexpected parameter: $className.$methodName.$parameterName")
    }

private fun getGenericParameterType(className: String, methodName: String, parameterName: String): String {
    return if (className == "NodeOrders" || methodName.endsWith("ForInt") || parameterName == "intData") {
        JS_INT
    } else {
        JS_DOUBLE
    }
}

private val INT_SIGNATURE_CLASSES = setOf(
    "IEnumerable",
    "List"
)

private fun correctEnumerable(types: List<JSONObject>) {
    types.asSequence()
        .filter { it[NAME] in INT_SIGNATURE_CLASSES }
        .flatMap { type ->
            sequenceOf(CONSTRUCTORS, METHODS)
                .filter { type.has(it) }
                .flatMap { type.flatMap(it) }
        }
        .optFlatMap(PARAMETERS)
        .filter { it.has(SIGNATURE) }
        .forEach {
            val signature = it[SIGNATURE]
            if (",$JS_NUMBER" in signature) {
                it[SIGNATURE] = signature.replace(",$JS_NUMBER", ",$JS_INT")
            }
        }
}
