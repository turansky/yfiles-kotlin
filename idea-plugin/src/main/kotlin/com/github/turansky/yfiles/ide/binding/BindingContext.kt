package com.github.turansky.yfiles.ide.binding

private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"
private const val LABEL_CONTEXT: String = "yfiles.styles.ILabelTemplateStyleBindingContext"

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
)

private val LABEL_CONTEXT_PARAMETERS = setOf(
    "isFlipped",
    "isUpsideDown",
    "labelText",
)

private val ALL_PARAMETERS = CONTEXT_PARAMETERS + LABEL_CONTEXT_PARAMETERS

internal fun getContextParameterParentClass(name: String?): String =
    if (name in LABEL_CONTEXT_PARAMETERS) LABEL_CONTEXT else CONTEXT

internal fun isValidContextParameter(name: String): Boolean =
    name in ALL_PARAMETERS
