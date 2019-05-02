package com.yworks.yfiles.api.generator

import org.json.JSONObject

private val INT = "Int"
private val DOUBLE = "Double"

internal fun correctNumbers(source: JSONObject) {
    val types = source
        .getJSONArray("namespaces")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("namespaces") }
        .flatMap { it.getJSONArray("namespaces").asSequence() }
        .map { it as JSONObject }
        .flatMap { it.getJSONArray("types").asSequence() }
        .map { it as JSONObject }
        .toList()

    correctEnumerable(types)

    types.asSequence()
        .onEach { it.correctConstants() }
        .onEach { it.correctConstructors() }
        .onEach { it.correctProperties() }
        .onEach { it.correctPropertiesGeneric() }
        .onEach { it.correctMethods() }
        .forEach { it.correctMethodParameters() }
}

private fun JSONObject.correctConstants() {
    if (!has("constants")) {
        return
    }

    val className = getString("name")
    getJSONArray("constants")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.getString("type") != JS_NUMBER }
        .filter { it.getString("type").contains(JS_NUMBER) }
        .forEach {
            if (it.has("signature")) {
                check(className == "HierarchicalClustering")
                it.put("signature", it.getString("signature").replace(",$JS_NUMBER>", ",$DOUBLE>"))
                return@forEach
            }

            val type = it.getString("type")
            check(type.endsWith("DpKey<$JS_NUMBER>"))

            val name = it.getString("name")
            val generic = if (name.contains("_ID_") || name.contains("_INDEX_")) {
                INT
            } else {
                DOUBLE
            }
            it.put("type", type.replace("<$JS_NUMBER>", "<$generic>"))
        }
}

private fun JSONObject.correctConstructors() {
    if (!has("constructors")) {
        return
    }

    val className = getString("name")
    getJSONArray("constructors")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("parameters") }
        .flatMap { it.getJSONArray("parameters").asSequence() }
        .map { it as JSONObject }
        .filter { it.getString("type") == JS_NUMBER }
        .forEach { it.put("type", getConstructorParameterType(className, it.getString("name"))) }
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
        return DOUBLE
    }

    return when (parameterName) {
        in INT_CONSTRUCTOR_PARAMETERS -> INT
        in DOUBLE_CONSTRUCTOR_PARAMETERS -> DOUBLE
        else -> getPropertyType(className, parameterName)
    }
}

private fun JSONObject.correctProperties() {
    correctProperties("staticProperties")
    correctProperties("properties")
}

private fun JSONObject.correctProperties(key: String) {
    if (!has(key)) {
        return
    }

    val className = getString("name")
    getJSONArray(key)
        .asSequence()
        .map { it as JSONObject }
        .filter { it.getString("type") == JS_NUMBER }
        .forEach { it.put("type", getPropertyType(className, it.getString("name"))) }
}

private fun getPropertyType(className: String, propertyName: String): String {
    if (propertyName.endsWith("Count")) {
        return INT
    }

    if (propertyName.endsWith("Cost")) {
        return DOUBLE
    }

    if (propertyName.endsWith("Ratio")) {
        return DOUBLE
    }

    if (className == "BalloonLayout" && propertyName == "minimumNodeDistance") {
        return INT
    }

    if (propertyName.endsWith("Distance")) {
        return DOUBLE
    }

    if (className == "AffineLine" && (propertyName == "a" || propertyName == "b")) {
        return DOUBLE
    }

    return when (propertyName) {
        in INT_PROPERTIES -> INT
        in DOUBLE_PROPERTIES -> DOUBLE
        else -> throw IllegalStateException("Unexpected $className.$propertyName")
    }
}

private fun JSONObject.correctPropertiesGeneric() {
    if (!has("properties")) {
        return
    }

    getJSONArray("properties")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.getString("type").contains(",$JS_NUMBER>") }
        .forEach { it.put("type", getPropertyGenericType(it.getString("name"), it.getString("type"))) }

    getJSONArray("properties")
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("signature") }
        .forEach {
            val signature = it.getString("signature")
            if (!signature.endsWith(",$JS_NUMBER>")) {
                return@forEach
            }

            val name = it.getString("name")
            check(name == "metric" || name == "heuristic")
            it.put("signature", signature.replace(",$JS_NUMBER>", ",$DOUBLE>"))
        }
}

private fun getPropertyGenericType(propertyName: String, type: String): String {
    val generic = if (
        propertyName.endsWith("Ids")
        || propertyName.endsWith("Indices")
        || propertyName.endsWith("Capacities", true)
        || propertyName == "busRootOffsets"
    ) {
        INT
    } else {
        DOUBLE
    }

    return type.replace(JS_NUMBER, generic)
}


private fun JSONObject.correctMethods() {
    correctMethods("staticMethods")
    correctMethods("methods")
}

private fun JSONObject.correctMethods(key: String) {
    if (!has(key)) {
        return
    }

    val className = getString("name")
    getJSONArray(key)
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("returns") }
        .forEach {
            val returns = it.getJSONObject("returns")
            if (returns.getString("type") == JS_NUMBER) {
                returns.put("type", getReturnType(className, it.getString("name")))
            }
        }
}

private fun getReturnType(className: String, methodName: String): String {
    if (methodName.endsWith("Count")) {
        return INT
    }

    if (methodName.endsWith("Components")) {
        return INT
    }

    if (methodName.endsWith("Cost") || methodName.endsWith("Costs")) {
        return DOUBLE
    }

    if (methodName.endsWith("Ratio")) {
        return DOUBLE
    }

    if (methodName.endsWith("Distance")) {
        return DOUBLE
    }

    if (className == "YVector" || className == "LineSegment" && methodName == "length") {
        return DOUBLE
    }

    return when (methodName) {
        in INT_METHODS -> INT
        in DOUBLE_METHODS -> DOUBLE
        else -> throw IllegalStateException("Unexpected $className.$methodName")
    }
}

private fun JSONObject.correctMethodParameters() {
    correctMethodParameters("staticMethods")
    correctMethodParameters("methods")
}

private fun JSONObject.correctMethodParameters(key: String) {
    if (!has(key)) {
        return
    }

    val className = getString("name")
    getJSONArray(key)
        .asSequence()
        .map { it as JSONObject }
        .filter { it.has("parameters") }
        .forEach { method ->
            val methodName = method.getString("name")
            method.getJSONArray("parameters")
                .asSequence()
                .map { it as JSONObject }
                .filter { it.getString("type") == JS_NUMBER }
                .forEach { it.put("type", getParameterType(className, methodName, it.getString("name"))) }
        }
}

private val A_MAP = mapOf(
    "fromArgb" to INT,
    "fromHSLA" to DOUBLE,
    "fromRGBA" to DOUBLE
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

private fun getParameterType(className: String, methodName: String, parameterName: String): String {
    if (methodName == "setInt" || methodName == "createHighPerformanceIntMap") {
        return INT
    }

    if (methodName in DOUBLE_METHOD_NAMES) {
        return DOUBLE
    }

    if (className in DOUBLE_CLASSES) {
        return DOUBLE
    }

    if (className == "List" || className == "IEnumerable") {
        return INT
    }

    if (parameterName.endsWith("Ratio")) {
        return DOUBLE
    }

    if (parameterName.endsWith("Duration")) {
        return DOUBLE
    }

    if (parameterName.endsWith("Distance")) {
        return DOUBLE
    }

    if (parameterName.endsWith("Index")) {
        return INT
    }

    if (parameterName.endsWith("Count")) {
        return INT
    }

    if (parameterName == "a") {
        return A_MAP.getValue(methodName)
    }

    return when (parameterName) {
        in INT_METHOD_PARAMETERS -> INT
        in INT_PROPERTIES -> INT
        in DOUBLE_METHOD_PARAMETERS -> DOUBLE
        in DOUBLE_PROPERTIES -> DOUBLE
        else -> throw IllegalStateException("Unexpected $className.$methodName.$parameterName")
    }
}

private val INT_SIGNATURE_CLASSES = setOf(
    "IEnumerable",
    "List"
)

private fun correctEnumerable(types: List<JSONObject>) {
    types.asSequence()
        .filter { it.getString("name") in INT_SIGNATURE_CLASSES }
        .flatMap { it.getJSONArray("methods").asSequence() }
        .map { it as JSONObject }
        .filter { it.has("parameters") }
        .flatMap { it.getJSONArray("parameters").asSequence() }
        .map { it as JSONObject }
        .filter { it.has("signature") }
        .forEach {
            val signature = it.getString("signature")
            if (signature.contains(",$JS_NUMBER")) {
                it.put("signature", signature.replace(",$JS_NUMBER", ",$INT"))
            }
        }
}

