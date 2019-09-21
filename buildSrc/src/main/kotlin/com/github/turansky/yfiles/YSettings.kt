package com.github.turansky.yfiles

private val YOBJECT_CLASS = "yfiles.lang.Object"

private val PRIMITIVE_CLASSES = setOf(
    "yfiles.lang.Boolean",
    "yfiles.lang.Number",
    "yfiles.lang.String"
)

private val MARKER_CLASSES = setOf(
    YOBJECT_CLASS,
    "yfiles.lang.EventArgs",
    "yfiles.lang.Attribute",
    "yfiles.algorithms.GraphObject",
    "yfiles.view.Visual"
)

fun isYObjectClass(className: String): Boolean {
    return className == YOBJECT_CLASS
}

fun isPrimitiveClass(className: String): Boolean {
    return className in PRIMITIVE_CLASSES
}

fun isMarkerClass(className: String): Boolean {
    return className in MARKER_CLASSES
}