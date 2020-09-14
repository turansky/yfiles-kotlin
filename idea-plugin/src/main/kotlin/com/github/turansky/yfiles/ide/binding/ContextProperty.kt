package com.github.turansky.yfiles.ide.binding

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons

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

private enum class ContextProperty(
    override val className: String = CONTEXT
) : IProperty {
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

private object InvalidContextProperty : IProperty {
    override val name: String
        get() = error("Name in unavailable!")

    override val className = CONTEXT
    override val isStandard = false
}

private val PROPERTY_MAP = ContextProperty.values()
    .associateBy { it.name }

internal fun findContextClass(propertyName: String?): String =
    PROPERTY_MAP[propertyName]?.className ?: CONTEXT

internal fun findContextProperty(name: String): IProperty =
    PROPERTY_MAP[name] ?: InvalidContextProperty

private fun ContextProperty.toVariant(): LookupElement =
    LookupElementBuilder.create(name)
        .withIcon(AllIcons.Nodes.Field)
