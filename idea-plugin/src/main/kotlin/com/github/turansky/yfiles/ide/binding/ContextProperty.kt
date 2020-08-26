package com.github.turansky.yfiles.ide.binding

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder

private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"
private const val LABEL_CONTEXT: String = "yfiles.styles.ILabelTemplateStyleBindingContext"

internal val CONTEXT_PROPERTY_VARIANTS: Array<out Any> by lazy {
    sequenceOf<IContextProperty>()
        .plus(ContextProperty.values())
        .plus(LabelContextProperty.values())
        .map { it.toVariant() }
        .toList()
        .toTypedArray()
}

internal interface IContextProperty {
    val name: String
    val className: String

    val isStandard: Boolean
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

    override val className = CONTEXT
    override val isStandard = true
}

private enum class LabelContextProperty : IContextProperty {
    isFlipped,
    isUpsideDown,
    labelText;

    override val className = LABEL_CONTEXT
    override val isStandard = true
}

private object ClassContextProperty : IContextProperty {
    override val name: String
        get() = TODO()

    override val className = CONTEXT
    override val isStandard = true
}

private object InvalidContextProperty : IContextProperty {
    override val name: String
        get() = TODO()

    override val className = CONTEXT
    override val isStandard = false
}

private val PROPERTY_MAP = sequenceOf<IContextProperty>()
    .plus(ContextProperty.values())
    .plus(LabelContextProperty.values())
    .associateBy { it.name }

internal fun findContextProperty(name: String?): IContextProperty {
    name ?: return ClassContextProperty

    return PROPERTY_MAP[name] ?: InvalidContextProperty
}

private fun IContextProperty.toVariant(): LookupElement =
    LookupElementBuilder.create(name)
