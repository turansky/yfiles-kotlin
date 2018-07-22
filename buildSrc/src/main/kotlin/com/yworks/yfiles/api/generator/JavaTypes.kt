package com.yworks.yfiles.api.generator

internal object JavaTypes {
    val VOID = "void"

    val STANDARD_TYPE_MAP = mapOf(
        "Object" to Types.OBJECT_TYPE,
        "object" to Types.OBJECT_TYPE,
        "boolean" to "boolean",
        "string" to "String",
        "number" to "double", // TODO: separate on int/double
        "Date" to "java.util.Date",
        "void" to VOID,
        "Function" to "Runnable",

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

        "WebGLProgram" to "com.google.gwt.webgl.client.WebGLProgram",
        "WebGLRenderingContext" to "com.google.gwt.webgl.client.WebGLRenderingContext",

        // TODO: check if Kotlin promises is what we need in yFiles
        "Promise" to "kotlin.js.Promise"
    )
}