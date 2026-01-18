package com.github.turansky.yfiles

internal val UNIT = Unit::class.simpleName!!
internal val ANY = Any::class.simpleName!!

internal val STRING: String = String::class.simpleName!!
internal val INT: String = Int::class.simpleName!!
internal val DOUBLE: String = Double::class.simpleName!!
internal val BOOLEAN: String = Boolean::class.simpleName!!

internal const val PROMISE = "js.promise.Promise"
internal const val PROMISE_RESULT = "js.promise.PromiseResult"
internal const val READ_ONLY_PROPERTY = "kotlin.properties.ReadOnlyProperty"
internal const val READ_WRITE_PROPERTY = "kotlin.properties.ReadWriteProperty"
internal const val KCLASS = "kotlin.reflect.KClass"
internal const val KPROPERTY = "kotlin.reflect.KProperty"

internal const val BLOB = "web.blob.Blob"

internal const val ELEMENT = "web.dom.Element"
internal const val HTML_ELEMENT = "web.html.HTMLElement"
internal const val SVG_ELEMENT = "web.svg.SVGElement"
internal const val SVG_SVG_ELEMENT = "web.svg.SVGSVGElement"

internal const val WEBGL2_RENDERING_CONTEXT = "web.gl.WebGL2RenderingContext"

internal fun getKotlinType(type: String): String? =
    STANDARD_TYPE_MAP[type]

private val STANDARD_TYPE_MAP = mapOf(
    JS_VOID to "js.core.Void",
    " unknown" to "*",

    JS_ANY to ANY,
    JS_OBJECT to ANY,
    JS_BOOLEAN to "Boolean",
    JS_STRING to STRING,
    JS_NUMBER to "Number",
    "Date" to "kotlin.js.Date",
    "Function" to "() -> $UNIT",

    "Record" to "js.objects.ReadonlyRecord",

    "Event" to "web.events.Event",
    "KeyboardEvent" to "web.keyboard.KeyboardEvent",

    "Document" to "web.dom.Document",
    "Node" to "web.dom.Node",
    JS_ELEMENT to ELEMENT,
    "HTMLElement" to HTML_ELEMENT,
    "HTMLInputElement" to "web.html.HTMLInputElement",
    "HTMLDivElement" to "web.html.HTMLDivElement",

    "ImageData" to "web.images.ImageData",
    "CanvasRenderingContext2D" to "web.canvas.CanvasRenderingContext2D",

    JS_SVG_ELEMENT to SVG_ELEMENT,
    JS_SVG_DEFS_ELEMENT to "web.svg.SVGDefsElement",
    "SVGGElement" to "web.svg.SVGGElement",
    "SVGImageElement" to "web.svg.SVGImageElement",
    "SVGPathElement" to "web.svg.SVGPathElement",
    "SVGTextElement" to "web.svg.SVGTextElement",
    JS_SVG_SVG_ELEMENT to SVG_SVG_ELEMENT,

    "WebGLProgram" to "web.gl.WebGLProgram",
    "WebGLRenderingContext" to "web.gl.WebGLRenderingContext",
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
