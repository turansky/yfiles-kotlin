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

internal enum class ContextProperty(
    override val className: String = CONTEXT,
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

    companion object {
        private val MAP = values()
            .associateBy { it.name }

        private val COMPLEX_MAP = listOf(
            bounds,
            canvasComponent,
            item,
            styleTag
        ).associateBy { it.name }

        internal fun findParentClass(propertyName: String?): String =
            MAP[propertyName]?.className ?: CONTEXT

        internal fun find(name: String): ContextProperty? =
            MAP[name]

        internal fun findComplex(name: String): ContextProperty? =
            COMPLEX_MAP[name]
    }
}

private fun ContextProperty.toVariant(): LookupElement =
    LookupElementBuilder.create(name)
        .withIcon(AllIcons.Nodes.Field)
