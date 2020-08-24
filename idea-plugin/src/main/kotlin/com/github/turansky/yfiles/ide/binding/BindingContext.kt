package com.github.turansky.yfiles.ide.binding

private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"
private const val LABEL_CONTEXT: String = "yfiles.styles.ILabelTemplateStyleBindingContext"

interface IContextParameter {
    val name: String
    val className: String
}

enum class ContextParameter : IContextParameter {
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

enum class LabelContextParameter : IContextParameter {
    isFlipped,
    isUpsideDown,
    labelText;

    override val className: String
        get() = LABEL_CONTEXT
}

private val PARAMETER_MAP = sequenceOf<IContextParameter>()
    .plus(ContextParameter.values())
    .plus(LabelContextParameter.values())
    .associateBy { it.name }

internal fun getContextParameterParentClass(name: String?): String {
    name ?: return CONTEXT

    return PARAMETER_MAP[name]?.className
        ?: CONTEXT
}

internal fun isValidContextParameter(name: String): Boolean =
    PARAMETER_MAP.containsKey(name)
