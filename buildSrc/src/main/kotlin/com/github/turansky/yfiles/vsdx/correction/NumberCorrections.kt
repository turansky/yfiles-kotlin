package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.JS_NUMBER
import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

private val INT = "Int"
private val DOUBLE = "Double"

private val INT_SUFFIXES = setOf(
    "Index",
    "Count",
    "Type"
)

private val DOUBLE_SUFFIXES = setOf(
    "X",
    "Y",
    "Width",
    "Height",

    "Scale",
    "Angle",
    "Factor",

    "Margin"
)

internal fun correctVsdxNumbers(source: JSONObject) {
    val types = VsdxSource(source)
        .types()
        .toList()

    types.asSequence()
        .onEach { it.correctProperties() }
        .forEach { it.correctMethodParameters() }
}

private fun JSONObject.correctProperties() {
    if (!has(J_PROPERTIES)) {
        return
    }

    val className = getString(J_NAME)
    jsequence(J_PROPERTIES)
        .forEach {
            val propertyName = it.getString(J_NAME)
            when (it.getString(J_TYPE)) {
                JS_NUMBER -> it.put(J_TYPE, getPropertyType(className, propertyName))
                "yfiles.vsdx.Value<$JS_NUMBER>" -> {
                    val generic = getPropertyType(className, propertyName)
                    it.put(J_TYPE, "yfiles.vsdx.Value<$generic>")
                }
            }
        }
}

private fun getPropertyType(className: String, propertyName: String): String {
    if (propertyName == "index") {
        return INT
    }

    if (INT_SUFFIXES.any { propertyName.endsWith(it) }) {
        return INT
    }

    if (className == "Scratch" || className == "VsdxPathSegment") {
        return DOUBLE
    }

    if (DOUBLE_SUFFIXES.any { propertyName.endsWith(it) }) {
        return DOUBLE
    }

    return "Number"
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
                        "Value<$JS_NUMBER>" -> {
                            val generic = getParameterType(className, methodName, parameterName)
                            it.put(J_TYPE, "Value<$generic>")
                        }
                    }
                }
        }
}

private fun getParameterType(
    className: String,
    methodName: String,
    parameterName: String
): String {
    if (methodName == "get" && parameterName == "i") {
        return INT
    }

    if (className == "VsdxPath") {
        return DOUBLE
    }

    return "Number"
}
