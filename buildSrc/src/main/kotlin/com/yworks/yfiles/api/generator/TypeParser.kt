package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE
import com.yworks.yfiles.api.generator.Types.UNIT

internal object TypeParser {
    private val FUNCTION_START = "function("
    private val FUNCTION_END = "):"
    private val FUNCTION_END_VOID = ")"

    private val GENERIC_START = "<"
    private val GENERIC_END = ">"

    private val STANDARD_TYPE_MAP = mapOf(
            "Object" to OBJECT_TYPE,
            "object" to OBJECT_TYPE,
            "boolean" to "Boolean",
            "string" to "String",
            "number" to "Number",
            "Date" to "kotlin.js.Date",
            "void" to UNIT,
            "Function" to "() -> ${UNIT}",

            "Event" to "org.w3c.dom.events.Event",
            "KeyboardEvent" to "org.w3c.dom.events.KeyboardEvent",
            "Document" to "org.w3c.dom.Document",
            "Node" to "org.w3c.dom.Node",
            "Element" to "org.w3c.dom.Element",
            "HTMLElement" to "org.w3c.dom.HTMLElement",
            "HTMLInputElement" to "org.w3c.dom.HTMLInputElement",
            "HTMLDivElement" to "org.w3c.dom.HTMLDivElement",
            "SVGElement" to "org.w3c.dom.svg.SVGElement",
            "SVGDefsElement" to "org.w3c.dom.svg.SVGDefsElement",
            "SVGGElement" to "org.w3c.dom.svg.SVGGElement",
            "SVGImageElement" to "org.w3c.dom.svg.SVGImageElement",
            "SVGPathElement" to "org.w3c.dom.svg.SVGPathElement",
            "SVGTextElement" to "org.w3c.dom.svg.SVGTextElement",
            "CanvasRenderingContext2D" to "org.w3c.dom.CanvasRenderingContext2D",

            // TODO: check if Kotlin promises is what we need in yFiles
            "Promise" to "kotlin.js.Promise"
    )

    fun parse(type: String): String {
        if (type.startsWith(FUNCTION_START)) {
            return parseFunctionType(type)
        }
        return parseType(type)
    }

    fun parseType(type: String): String {
        // TODO: Fix for SvgDefsManager and SvgVisual required (2 constructors from 1)
        if (type.contains("|")) {
            return parseType(type.split("|")[0])
        }

        val standardType = STANDARD_TYPE_MAP[type]
        if (standardType != null) {
            return standardType
        }

        if (!type.contains(GENERIC_START)) {
            return type
        }

        val mainType = parseType(till(type, GENERIC_START))
        val parametrizedTypes = parseGenericParameters(between(type, GENERIC_START, GENERIC_END))
        return "$mainType<${parametrizedTypes.joinToString(", ")}>"
    }

    fun getGenericString(parameters: List<TypeParameter>): String {
        return if (parameters.isEmpty()) "" else "<${parameters.map { it.name }.joinToString(", ")}> "
    }

    fun parseGenericParameters(parameters: String): List<String> {
        // TODO: temp hack for generic, logic check required
        if (!parameters.contains(GENERIC_START)) {
            return if (parameters.contains(FUNCTION_START)) {
                // TODO: realize full logic if needed
                parameters.split(delimiters = *arrayOf(","), limit = 2).map {
                    if (it.startsWith(FUNCTION_START)) parseFunctionType(it) else parseType(it)
                }
            } else {
                parameters.split(",").map { parseType(it) }
            }
        }

        val firstType = firstGenericType(parameters)
        if (firstType == parameters) {
            return listOf(parseType(firstType))
        }

        val types = mutableListOf(firstType)
        types.addAll(parseGenericParameters(parameters.substring(firstType.length + 1)))
        return types.toList()
    }

    fun firstGenericType(parameters: String): String {
        var semafor = 0
        var index = 0

        while (true) {
            val indexes = listOf(
                    parameters.indexOf(",", index),
                    parameters.indexOf("<", index),
                    parameters.indexOf(">", index)
            )

            if (indexes.all { it == -1 }) {
                return parameters
            }

            // TODO: check calculation
            index = indexes.map({ if (it == -1) 100000 else it })
                    .minWith(Comparator { o1, o2 -> Math.min(o1, o2) }) ?: -1

            if (index == -1 || index == parameters.lastIndex) {
                return parameters
            }

            when (indexes.indexOf(index)) {
                0 -> if (semafor == 0) return parameters.substring(0, index)
                1 -> semafor++
                2 -> semafor--
            }
            index++
        }
    }

    fun parseFunctionType(type: String): String {
        val voidResult = type.endsWith(FUNCTION_END_VOID)
        val functionEnd = if (voidResult) FUNCTION_END_VOID else FUNCTION_END
        val parameterTypes = between(type, FUNCTION_START, functionEnd)
                .split(",").map({ parseType(it) })
        val resultType = if (voidResult) UNIT else parseType(from(type, FUNCTION_END))
        return "(${parameterTypes.joinToString(", ")}) -> $resultType"
    }
}