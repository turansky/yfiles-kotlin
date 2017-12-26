package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE
import com.yworks.yfiles.api.generator.Types.UNIT

internal object TypeParser {
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

    fun parse(type: String, signature: String?): String {
        return parseType(signature ?: type)
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
        return if (parameters.isNotEmpty()) {
            "<${parameters.map { it.name }.joinToString(", ")}> "
        } else {
            ""
        }
    }

    // TODO: optimize calculation
    private fun parseGenericParameters(parameters: String): List<String> {
        if (!parameters.contains(GENERIC_START)) {
            return parameters.split(",")
                    .map { parseType(it) }
        }

        val result = mutableListOf<String>()

        var items = emptyList<String>()
        parameters.split(",").forEach { part ->
            items += part
            val str = items.joinToString(",")
            if (str.count { it.equals('<') } == str.count { it.equals('>') }) {
                result.add(parseType(str))
                items = emptyList()
            }
        }

        return result.toList()
    }
}