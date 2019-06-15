package com.github.turansky.yfiles

private val PRIMITIVE_CLASSES = setOf(
    "yfiles.lang.Boolean",
    "yfiles.lang.Number",
    "yfiles.lang.Object",
    "yfiles.lang.String"
)

private val MARKER_CLASSES = setOf(
    "yfiles.lang.EventArgs",
    "yfiles.lang.Attribute",
    "yfiles.algorithms.GraphObject",
    "yfiles.view.Visual"
)

fun isPrimitiveClass(className: String): Boolean {
    return className in PRIMITIVE_CLASSES
}

fun isMarkerClass(className: String): Boolean {
    return className in MARKER_CLASSES
}