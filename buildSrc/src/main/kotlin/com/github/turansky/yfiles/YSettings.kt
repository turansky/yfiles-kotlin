package com.github.turansky.yfiles

const val YOBJECT = "yfiles.lang.YObject"

private const val YOBJECT_CLASS = "yfiles.lang.Object"

private val PRIMITIVE_CLASSES = setOf(
    "yfiles.lang.Boolean",
    "yfiles.lang.Number",
    "yfiles.lang.String"
)

private val MARKER_CLASSES = setOf(
    YOBJECT_CLASS,
    "yfiles.lang.EventArgs",
    "yfiles.lang.Attribute",
    "yfiles.algorithms.GraphObject"
)

fun isYObjectClass(className: String): Boolean =
    className == YOBJECT_CLASS

fun isPrimitiveClass(className: String): Boolean =
    className in PRIMITIVE_CLASSES

fun isMarkerClass(className: String): Boolean =
    className in MARKER_CLASSES
