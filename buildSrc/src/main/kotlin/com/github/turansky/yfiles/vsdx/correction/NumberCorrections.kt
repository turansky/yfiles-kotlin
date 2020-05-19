package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.JS_DOUBLE
import com.github.turansky.yfiles.JS_INT
import com.github.turansky.yfiles.JS_NUMBER
import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

private val INT_NAMES = setOf(
    "id",
    "index",

    "page",
    "container",
    "master",
    "sheet",
    "bullet",

    "background",
    "uiVisibility",
    "walkPreference",
    "flags",

    "type",
    "case",
    "style",
    "langID",
    "themeEffects",

    "action",
    "calendar",
    "format"
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
    "Character",
    "Control",
    "CoordinateConverter",
    "Paragraph",
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
        .onEach { it.correctMethods() }
        .onEach { it.correctProperties() }
        .forEach { it.correctMethodParameters() }
}

private fun getType(name: String): String? =
    when {
        name.startsWith("show") -> JS_INT

        name in INT_NAMES -> JS_INT
        name in DOUBLE_NAMES -> JS_DOUBLE

        INT_SUFFIXES.any { name.endsWith(it) } -> JS_INT
        DOUBLE_SUFFIXES.any { name.endsWith(it) } -> JS_DOUBLE

        else -> null
    }

private fun JSONObject.correctProperties() {
    if (!has(PROPERTIES)) {
        return
    }

    val className = get(NAME)
    flatMap(PROPERTIES)
        .forEach {
            val propertyName = it[NAME]
            when (it[TYPE]) {
                JS_NUMBER -> it[TYPE] = getPropertyType(className, propertyName)
                "yfiles.vsdx.Value<$JS_NUMBER>" -> {
                    val generic = getPropertyType(className, propertyName)
                    it[TYPE] = "yfiles.vsdx.Value<$generic>"
                }
            }
        }
}

private fun getPropertyType(className: String, propertyName: String): String {
    getType(propertyName)?.also {
        return it
    }

    if (className in INT_CLASSES) {
        return JS_INT
    }

    if (className in DOUBLE_CLASSES) {
        return JS_DOUBLE
    }

    throw IllegalStateException("Unexpected $className.$propertyName")
}

private fun JSONObject.correctMethodParameters() {
    val className = get(NAME)
    optFlatMap(METHODS)
        .filter { it.has(PARAMETERS) }
        .forEach { method ->
            val methodName = method[NAME]
            method.flatMap(PARAMETERS)
                .forEach {
                    val parameterName = it[NAME]
                    when (it[TYPE]) {
                        JS_NUMBER -> it[TYPE] = getParameterType(className, methodName, parameterName)
                        "Value<$JS_NUMBER>", "yfiles.vsdx.Value<$JS_NUMBER>" -> {
                            val generic = getParameterType(className, methodName, parameterName)
                            it[TYPE] = "Value<$generic>"
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

    return when {
        methodName == "get" && parameterName == "i" -> JS_INT
        methodName == "enum" || methodName == "rgb" -> JS_INT

        className in DOUBLE_CLASSES -> JS_DOUBLE

        else -> throw IllegalStateException("Unexpected $className.$methodName.$parameterName")
    }
}

private fun JSONObject.correctMethods() {
    val className = get(NAME)
    optFlatMap(METHODS)
        .filter { it.has(RETURNS) }
        .forEach {
            val methodName = it[NAME]
            it[RETURNS].apply {
                when (get(TYPE)) {
                    JS_NUMBER -> set(TYPE, getReturnType(className, methodName))
                    "Value<$JS_NUMBER>", "yfiles.vsdx.Value<$JS_NUMBER>" -> {
                        val generic = getReturnType(className, methodName)
                        set(TYPE, "Value<$generic>")
                    }
                }
            }
        }
}

private fun getReturnType(
    className: String,
    methodName: String
): String =
    when {
        methodName == "enum" -> JS_INT

        className == "CoordinateConverter" -> JS_DOUBLE
        className == "Value" -> JS_DOUBLE

        methodName == "getGradientAngle" -> JS_DOUBLE
        methodName == "getGradientDir" -> JS_INT

        methodName == "toVsdxTransparency" -> JS_DOUBLE

        else -> throw IllegalStateException("Unexpected $className.$methodName")
    }
