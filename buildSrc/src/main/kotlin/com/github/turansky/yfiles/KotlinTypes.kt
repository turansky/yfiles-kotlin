package com.github.turansky.yfiles

internal val UNIT = Unit::class.simpleName!!
internal val ANY = Any::class.simpleName!!

internal val STRING: String = String::class.simpleName!!
internal val INT: String = Int::class.simpleName!!
internal val DOUBLE: String = Double::class.simpleName!!
internal val BOOLEAN: String = Boolean::class.simpleName!!

internal const val PROMISE = "kotlin.js.Promise"
internal const val PROMISE_RESULT = "kotlinx.js.PromiseResult"
internal const val READ_ONLY_PROPERTY = "kotlin.properties.ReadOnlyProperty"
internal const val READ_WRITE_PROPERTY = "kotlin.properties.ReadWriteProperty"
internal const val KCLASS = "kotlin.reflect.KClass"
internal const val KPROPERTY = "kotlin.reflect.KProperty"

internal const val BLOB = "web.buffer.Blob"

internal const val ELEMENT = "dom.Element"
internal const val HTML_ELEMENT = "dom.html.HTMLElement"
internal const val SVG_ELEMENT = "dom.svg.SVGElement"
internal const val SVG_SVG_ELEMENT = "dom.svg.SVGSVGElement"

internal const val WEBGL2_RENDERING_CONTEXT = "webgl.WebGL2RenderingContext"

internal fun getKotlinType(type: String): String? =
    STANDARD_TYPE_MAP[type]

private val STANDARD_TYPE_MAP = mapOf(
    JS_VOID to "kotlinx.js.Void",
    " unknown" to "*",

    JS_ANY to ANY,
    JS_OBJECT to ANY,
    JS_BOOLEAN to "Boolean",
    JS_STRING to STRING,
    JS_NUMBER to "Number",
    "Date" to "kotlin.js.Date",
    "Function" to "() -> $UNIT",

    "Record" to "kotlinx.js.Record",

    "Event" to "web.events.Event",
    "KeyboardEvent" to "dom.events.KeyboardEvent",

    "Document" to "dom.Document",
    "Node" to "dom.Node",
    JS_ELEMENT to ELEMENT,
    "HTMLElement" to HTML_ELEMENT,
    "HTMLInputElement" to "dom.html.HTMLInputElement",
    "HTMLDivElement" to "dom.html.HTMLDivElement",

    "ImageData" to "canvas.ImageData",
    "CanvasRenderingContext2D" to "canvas.CanvasRenderingContext2D",

    JS_SVG_ELEMENT to SVG_ELEMENT,
    JS_SVG_DEFS_ELEMENT to "dom.svg.SVGDefsElement",
    "SVGGElement" to "dom.svg.SVGGElement",
    "SVGImageElement" to "dom.svg.SVGImageElement",
    "SVGPathElement" to "dom.svg.SVGPathElement",
    "SVGTextElement" to "dom.svg.SVGTextElement",
    JS_SVG_SVG_ELEMENT to SVG_SVG_ELEMENT,

    "WebGLProgram" to "webgl.WebGLProgram",
    "WebGLRenderingContext" to "webgl.WebGLRenderingContext",
    "WebGL2RenderingContext" to WEBGL2_RENDERING_CONTEXT,

    JS_BLOB to BLOB,

    "Promise" to PROMISE,
    "PromiseResult" to PROMISE_RESULT,
    JS_CLASS to "kotlin.js.JsClass"
)

val STANDARD_IMPORTED_TYPES = STANDARD_TYPE_MAP
    .values
    .asSequence()
    .filter { "." in it }
    .plus(READ_ONLY_PROPERTY)
    .plus(READ_WRITE_PROPERTY)
    .plus(KCLASS)
    .plus(KPROPERTY)
    .toSet()
