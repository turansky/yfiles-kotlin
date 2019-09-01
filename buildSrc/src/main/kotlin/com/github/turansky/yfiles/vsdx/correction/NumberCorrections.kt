package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.JS_NUMBER
import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

private val INT = "Int"
private val DOUBLE = "Double"

private val INT_NAMES = setOf(
    "id",
    "index",

    "page",
    "container",
    "master",
    "sheet",

    "background",
    "uiVisibility",
    "walkPreference",
    "flags",

    "type",
    "case"
)

private val INT_SUFFIXES = setOf(
    "Index",
    "Count",
    "Type",
    "Code",

    "Window",
    "Page",
    "Format",
    "Style",

    "Angles",
    "Extensions",
    "Level",
    "Settings",
    "Ext",
    "Pos",

    "Group",
    "State",

    "Orientation",
    "Pattern",
    "Cap",
    "Arrow",
    "Alignment",
    "Font",
    "Dir"
)

private val INT_CLASSES = setOf(
    "Field"
)

private val DOUBLE_NAMES = setOf(
    "x",
    "y",
    "width",
    "height",

    "angle",
    "alpha",
    "scale",
    "zoom",
    "position",
    "stopPosition",

    "number",
    "radius",
    "radians",
    "inches",
    "transparency"
)

private val DOUBLE_SUFFIXES = setOf(
    "X",
    "Y",
    "Width",
    "Height",

    "Left",
    "Top",

    "Scale",
    "Angle",
    "Factor",
    "Margin",
    "Transparency",

    "Origin",
    "Density",
    "Spacing",
    "Weight",
    "Size"
)

private val DOUBLE_CLASSES = setOf(
    "Control",
    "CoordinateConverter",
    "Scratch",
    "Stylable",
    "VsdxPath",
    "VsdxPathSegment"
)

internal fun correctVsdxNumbers(source: JSONObject) {
    val types = VsdxSource(source)
        .types()
        .toList()

    types.asSequence()
        .onEach { it.correctProperties() }
        .forEach { it.correctMethodParameters() }
}

private fun getType(name: String): String? {
    if (name.startsWith("show")) {
        return INT
    }

    if (name in INT_NAMES) {
        return INT
    }

    if (name in DOUBLE_NAMES) {
        return DOUBLE
    }

    if (INT_SUFFIXES.any { name.endsWith(it) }) {
        return INT
    }

    if (DOUBLE_SUFFIXES.any { name.endsWith(it) }) {
        return DOUBLE
    }

    return null
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
    getType(propertyName)?.also {
        return it
    }

    if (className in INT_CLASSES) {
        return INT
    }

    if (className in DOUBLE_CLASSES) {
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
    getType(parameterName)?.also {
        return it
    }

    if (methodName == "get" && parameterName == "i") {
        return INT
    }

    if (methodName == "enum" || methodName == "rgb") {
        return INT
    }

    if (className in DOUBLE_CLASSES) {
        return DOUBLE
    }

    return "Number"
}
