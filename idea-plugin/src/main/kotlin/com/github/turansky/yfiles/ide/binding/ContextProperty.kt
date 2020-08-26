package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.ContextProperty.Type.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder

private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"
private const val LABEL_CONTEXT: String = "yfiles.styles.ILabelTemplateStyleBindingContext"

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
    val type: Type,
    override val className: String = CONTEXT
) : IContextProperty {
    bounds(RECT),
    canvasComponent(CANVAS_COMPONENT),
    height(DOUBLE),
    item(IMODEL_ITEM),
    itemFocused(BOOLEAN),
    itemHighlighted(BOOLEAN),
    itemSelected(BOOLEAN),
    styleTag(STYLE_TAG),
    width(DOUBLE),
    zoom(DOUBLE),

    isFlipped(BOOLEAN, LABEL_CONTEXT),
    isUpsideDown(BOOLEAN, LABEL_CONTEXT),
    labelText(STRING, LABEL_CONTEXT);

    override val isStandard: Boolean = true

    enum class Type {
        STRING,
        DOUBLE,
        BOOLEAN,

        RECT,
        IMODEL_ITEM,
        CANVAS_COMPONENT,

        STYLE_TAG;

        private val value = name.split("_")
            .joinToString("") { it.toLowerCase().capitalize() }

        override fun toString(): String = value
    }
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

private val PROPERTY_MAP = ContextProperty.values()
    .associateBy { it.name }

internal fun findContextProperty(name: String?): IContextProperty {
    name ?: return ClassContextProperty

    return PROPERTY_MAP[name] ?: InvalidContextProperty
}

private fun ContextProperty.toVariant(): LookupElement =
    LookupElementBuilder.create(name)
        .withTypeText(type.toString(), true)
