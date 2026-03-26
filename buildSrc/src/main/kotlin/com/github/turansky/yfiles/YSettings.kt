package com.github.turansky.yfiles

private val PRIMITIVE_CLASSES = setOf(
    "yfiles.lang.Boolean",
    "yfiles.lang.Number",
    "yfiles.lang.String"
)

private val MARKER_CLASSES = setOf(
    "yfiles.lang.EventArgs",
    "yfiles.lang.Attribute",
    GRAPH_OBJECT
)

fun isPrimitiveClass(className: String): Boolean =
    className in PRIMITIVE_CLASSES

fun isMarkerClass(className: String): Boolean =
    className in MARKER_CLASSES
