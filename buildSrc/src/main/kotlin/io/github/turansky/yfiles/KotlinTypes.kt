package io.github.turansky.yfiles

internal object KotlinTypes {
    val UNIT = "Unit"

    val STANDARD_TYPE_MAP = mapOf(
        JS_ANY to "Any",
        JS_OBJECT to "Any",
        JS_BOOLEAN to "Boolean",
        JS_STRING to "String",
        JS_NUMBER to "Number",
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

        "WebGLProgram" to "org.khronos.webgl.WebGLProgram",
        "WebGLRenderingContext" to "org.khronos.webgl.WebGLRenderingContext",

        "Promise" to "kotlin.js.Promise"
    )
}