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

        "Event" to "com.google.gwt.dom.client.NativeEvent",
        "KeyboardEvent" to "com.google.gwt.dom.client.NativeEvent",
        "Document" to "com.google.gwt.dom.client.Document",
        "Node" to "com.google.gwt.dom.client.Node",
        "Element" to "com.google.gwt.dom.client.Element",
        "HTMLElement" to "com.google.gwt.dom.client.Element",
        "HTMLInputElement" to "com.google.gwt.dom.client.InputElement",
        "HTMLDivElement" to "com.google.gwt.dom.client.DivElement",
        "SVGElement" to "com.google.gwt.dom.client.Element",
        "SVGDefsElement" to "com.google.gwt.dom.client.Element",
        "SVGGElement" to "com.google.gwt.dom.client.Element",
        "SVGImageElement" to "com.google.gwt.dom.client.Element",
        "SVGPathElement" to "com.google.gwt.dom.client.Element",
        "SVGTextElement" to "com.google.gwt.dom.client.Element",
        "CanvasRenderingContext2D" to "com.google.gwt.canvas.dom.client.Context2d",

        "WebGLProgram" to Types.OBJECT_TYPE,
        "WebGLRenderingContext" to Types.OBJECT_TYPE,

        // TODO: add promise alias
        "Promise" to "java.util.Consumer"
    )
}