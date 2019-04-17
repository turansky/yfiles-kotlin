package com.yworks.yfiles.api.generator

private val MARKER_CLASS = sequenceOf(
    "yfiles.lang.EventArgs",
    "yfiles.lang.Attribute",
    "yfiles.algorithms.GraphObject",
    "yfiles.view.Visual"
)
    .map(::fixPackage)
    .toSet()

fun isMarkerClass(className: String): Boolean {
    return className in MARKER_CLASS
}