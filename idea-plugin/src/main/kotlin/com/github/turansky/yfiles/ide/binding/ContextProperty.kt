package com.github.turansky.yfiles.ide.binding

private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"
private const val LABEL_CONTEXT: String = "yfiles.styles.ILabelTemplateStyleBindingContext"

interface IContextProperty {
    val name: String
    val className: String
}

private enum class ContextProperty : IContextProperty {
    bounds,
    canvasComponent,
    height,
    item,
    itemFocused,
    itemHighlighted,
    itemSelected,
    styleTag,
    width,
    zoom;

    override val className: String
        get() = CONTEXT
}

private enum class LabelContextProperty : IContextProperty {
    isFlipped,
    isUpsideDown,
    labelText;

    override val className: String
        get() = LABEL_CONTEXT
}

private val PARAMETER_MAP = sequenceOf<IContextProperty>()
    .plus(ContextProperty.values())
    .plus(LabelContextProperty.values())
    .associateBy { it.name }

internal fun getContextParameterParentClass(name: String?): String {
    name ?: return CONTEXT

    return PARAMETER_MAP[name]?.className
        ?: CONTEXT
}

internal fun isValidContextParameter(name: String): Boolean =
    PARAMETER_MAP.containsKey(name)
