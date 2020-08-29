package com.github.turansky.yfiles.ide.binding

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder

private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"
private const val LABEL_CONTEXT: String = "yfiles.styles.ILabelTemplateStyleBindingContext"

internal val CONTEXT_CLASSES: List<String> = listOf(
    CONTEXT,
    LABEL_CONTEXT
)

internal val CONTEXT_PROPERTY_VARIANTS: Array<out Any> by lazy {
    ContextProperty.values()
        .map { it.toVariant() }
        .toTypedArray()
}

internal interface IContextProperty {
    val name: String
    val className: String

    val isStandard: Boolean
}

private enum class ContextProperty(
    override val className: String = CONTEXT
) : IContextProperty {
    bounds,
    canvasComponent,
    height,
    item,
    itemFocused,
    itemHighlighted,
    itemSelected,
    styleTag,
    width,
    zoom,

    isFlipped(LABEL_CONTEXT),
    isUpsideDown(LABEL_CONTEXT),
    labelText(LABEL_CONTEXT);

    override val isStandard: Boolean = true
}

private object ClassContextProperty : IContextProperty {
    override val name: String
        get() = error("Name in unavailable!")

    override val className = CONTEXT
    override val isStandard = true
}

private object InvalidContextProperty : IContextProperty {
    override val name: String
        get() = error("Name in unavailable!")

    override val className = CONTEXT
    override val isStandard = false
}

private val PROPERTY_MAP = ContextProperty.values()
    .associateBy { it.name }

internal fun findContextProperty(name: String?): IContextProperty {
    name ?: return ClassContextProperty

    return PROPERTY_MAP[name] ?: InvalidContextProperty
}

private fun ContextProperty.toVariant(): LookupElement =
    LookupElementBuilder.create(name)
