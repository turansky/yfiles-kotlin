package com.github.turansky.yfiles.correction

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
        .getJSONObject(J_FUNCTION_SIGNATURES)
        .getJSONObject("yfiles.view.AnimationCallback")
        .getJSONArray(J_PARAMETERS)
        .single() as JSONObject)
        .put(J_TYPE, JS_DOUBLE)

}

private fun JSONObject.correctConstants() {
    if (!has("constants")) {
        return
    }

    val className = getString(J_NAME)
    jsequence("constants")
        .filter { it.getString(J_TYPE) != JS_NUMBER }
        .filter { it.getString(J_TYPE).contains(JS_NUMBER) }
        .forEach {
            if (it.has(J_SIGNATURE)) {
                check(className == "HierarchicalClustering")
                it.put(J_SIGNATURE, it.getString("signature").replace(",$JS_NUMBER>", ",$JS_DOUBLE>"))
                return@forEach
            }

            val type = it.getString(J_TYPE)
            check(type.endsWith("DpKey<$JS_NUMBER>"))

            val name = it.getString(J_NAME)
            val generic = if (name.contains("_ID_") || name.contains("_INDEX_")) {
                JS_INT
            } else {
                JS_DOUBLE
            }
            it.put(J_TYPE, type.replace("<$JS_NUMBER>", "<$generic>"))
        }
}

private fun JSONObject.correctConstructors() {
    if (!has(J_CONSTRUCTORS)) {
        return
    }

    val className = getString(J_NAME)
    jsequence(J_CONSTRUCTORS)
        .optionalArray(J_PARAMETERS)
        .filter { it.getString(J_TYPE) == JS_NUMBER }
        .forEach { it.put(J_TYPE, getConstructorParameterType(className, it.getString(J_NAME))) }

    jsequence(J_CONSTRUCTORS)
        .optionalArray(J_PARAMETERS)
        .filter { it.getString(J_TYPE) == "Array<$JS_NUMBER>" }
        .forEach {
            val genericType = when (val name = it.getString(J_NAME)) {
                "dashes" -> JS_DOUBLE
                "rowLayout", "columnLayout" -> JS_INT
                else -> throw IllegalStateException("Unexpected constructor parameter $name")
            }

            it.put(J_TYPE, "Array<$genericType>")
        }
}

private val DOUBLE_CONSTRUCTOR_CLASSES = setOf(
    "BorderLine",
    "GridConstraintProvider",
    "YVector",
    "Matrix",
    "TimeSpan",
    "DefaultNodePlacer",
    "Interval",
    "MinimumNodeSizeStage",
    "FreeEdgeLabelLayoutModelParameter",
    "FreeNodeLabelLayoutModelParameter"
)

private fun getConstructorParameterType(className: String, parameterName: String): String {
    if (className in DOUBLE_CONSTRUCTOR_CLASSES) {
        return JS_DOUBLE
    }

    return when (parameterName) {
        in INT_CONSTRUCTOR_PARAMETERS -> JS_INT
        in DOUBLE_CONSTRUCTOR_PARAMETERS -> JS_DOUBLE
        else -> getPropertyType(className, parameterName)
    }
}

private fun JSONObject.correctProperties() {
    correctProperties("staticProperties")
    correctProperties(J_PROPERTIES)
}

private fun JSONObject.correctProperties(key: String) {
    if (!has(key)) {
        return
    }

    val className = getString(J_NAME)
    jsequence(key)
        .filter { it.getString(J_TYPE) == JS_NUMBER }
        .forEach { it.put(J_TYPE, getPropertyType(className, it.getString(J_NAME))) }
}

private fun getPropertyType(className: String, propertyName: String): String =
    when {
        propertyName.endsWith("Count") -> JS_INT
        propertyName.endsWith("Cost") -> JS_DOUBLE
        propertyName.endsWith("Ratio") -> JS_DOUBLE

        className == "BalloonLayout" && propertyName == "minimumNodeDistance" -> JS_INT

        propertyName.endsWith("Distance") -> JS_DOUBLE

        className == "AffineLine" && (propertyName == "a" || propertyName == "b") -> JS_DOUBLE

        propertyName in INT_PROPERTIES -> JS_INT
        propertyName in DOUBLE_PROPERTIES -> JS_DOUBLE

        else -> throw IllegalStateException("Unexpected $className.$propertyName")
    }

private fun JSONObject.correctPropertiesGeneric() {
    if (!has(J_PROPERTIES)) {
        return
    }

    jsequence(J_PROPERTIES)
        .filter { it.getString(J_TYPE).contains("$JS_NUMBER>") }
        .forEach { it.put(J_TYPE, getPropertyGenericType(it.getString(J_NAME), it.getString(J_TYPE))) }

    jsequence(J_PROPERTIES)
        .filter { it.has(J_SIGNATURE) }
        .forEach {
            val signature = it.getString(J_SIGNATURE)
            if (!signature.endsWith(",$JS_NUMBER>")) {
                return@forEach
            }

            val name = it.getString(J_NAME)
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
    correctMethods(J_STATIC_METHODS)
    correctMethods(J_METHODS)
}

private fun JSONObject.correctMethods(key: String) {
    if (!has(key)) {
        return
    }

    val className = getString(J_NAME)
    jsequence(key)
        .filter { it.has(J_RETURNS) }
        .forEach {
            val methodName = it.getString(J_NAME)
            val returns = it.getJSONObject(J_RETURNS)

            when (returns.getString(J_TYPE)) {
                JS_NUMBER -> returns.put(J_TYPE, getReturnType(className, methodName))
                "Array<$JS_NUMBER>" -> returns.put(J_TYPE, "Array<$JS_DOUBLE>")
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

        className == "YVector" || className == "LineSegment" && methodName == "length" -> JS_DOUBLE

        methodName in INT_METHODS -> JS_INT
        methodName in DOUBLE_METHODS -> JS_DOUBLE

        else -> throw IllegalStateException("Unexpected $className.$methodName")
    }

private fun JSONObject.correctMethodParameters() {
    correctMethodParameters(J_STATIC_METHODS)
    correctMethodParameters(J_METHODS)
}

private fun JSONObject.correctMethodParameters(key: String) {
    if (!has(key)) {
        return
    }

    val className = getString(J_NAME)
    jsequence(key)
        .filter { it.has(J_PARAMETERS) }
        .forEach { method ->
            val methodName = method.getString(J_NAME)
            method.jsequence(J_PARAMETERS)
                .forEach {
                    val parameterName = it.getString(J_NAME)
                    when (it.getString(J_TYPE)) {
                        JS_NUMBER -> it.put(J_TYPE, getParameterType(className, methodName, parameterName))
                        "Array<$JS_NUMBER>" -> {
                            val generic = getGenericParameterType(className, methodName, parameterName)
                            it.put(J_TYPE, "Array<$generic>")
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

        parameterName.endsWith("Index") -> JS_INT
        parameterName.endsWith("Count") -> JS_INT

        parameterName == "a" -> A_MAP.getValue(methodName)

        parameterName in INT_METHOD_PARAMETERS -> JS_INT
        parameterName in INT_PROPERTIES -> JS_INT

        parameterName in DOUBLE_METHOD_PARAMETERS -> JS_DOUBLE
        parameterName in DOUBLE_PROPERTIES -> JS_DOUBLE

        else -> throw IllegalStateException("Unexpected $className.$methodName.$parameterName")
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
        .filter { it.getString(J_NAME) in INT_SIGNATURE_CLASSES }
        .flatMap { type ->
            sequenceOf(J_CONSTRUCTORS, J_METHODS, J_STATIC_METHODS)
                .filter { type.has(it) }
                .flatMap { type.jsequence(it) }
        }
        .optionalArray(J_PARAMETERS)
        .filter { it.has("signature") }
        .forEach {
            val signature = it.getString("signature")
            if (signature.contains(",$JS_NUMBER")) {
                it.put("signature", signature.replace(",$JS_NUMBER", ",$JS_INT"))
            }
        }
}

