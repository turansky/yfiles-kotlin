package com.github.turansky.yfiles.ide.binding

private val CONTEXT_PARAMETERS = setOf(
    "bounds",
    "canvasComponent",
    "height",
    "item",
    "itemFocused",
    "itemHighlighted",
    "itemSelected",
    "styleTag",
    "width",
    "zoom",

    "isFlipped",
    "isUpsideDown",
    "labelText",
)

internal fun isContextParameter(name: String): Boolean =
    name in CONTEXT_PARAMETERS
