package com.yworks.yfiles.api.generator

internal object JavaTypes {
    val VOID = "void"

    val STANDARD_TYPE_MAP = mapOf(
        "Object" to Types.OBJECT_TYPE,
        "object" to Types.OBJECT_TYPE,
        JS_BOOLEAN to "boolean",
        JS_STRING to "String",
        JS_NUMBER to "double", // TODO: separate on int/double
        "Date" to "elemental2.core.JsDate",
        "void" to VOID,
        "Function" to "elemental2.core.Function",

        "Event" to "elemental2.dom.Event",
        "KeyboardEvent" to "elemental2.dom.KeyboardEvent",
        "Document" to "elemental2.dom.Document",
        "Node" to "elemental2.dom.Node",
        "Element" to "elemental2.dom.Element",
        "HTMLElement" to "elemental2.dom.HTMLElement",
        "HTMLInputElement" to "elemental2.dom.HTMLInputElement",
        "HTMLDivElement" to "elemental2.dom.HTMLDivElement",
        "SVGElement" to "elemental2.svg.SVGElement",
        "SVGDefsElement" to "elemental2.svg.SVGDefsElement",
        "SVGGElement" to "elemental2.svg.SVGGElement",
        "SVGImageElement" to "elemental2.svg.SVGImageElement",
        "SVGPathElement" to "elemental2.svg.SVGPathElement",
        "SVGTextElement" to "elemental2.svg.SVGTextElement",
        "CanvasRenderingContext2D" to "elemental2.dom.CanvasRenderingContext2D",

        "WebGLProgram" to "elemental2.webgl.WebGLProgram",
        "WebGLRenderingContext" to "elemental2.webgl.WebGLRenderingContext",

        "Promise" to "elemental2.promise.Promise",
        // TODO: add comparator alias
        "Comparator" to "java.util.Comparator"
    )
}