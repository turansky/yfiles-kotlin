package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.ExistedContextProperty.Type
import com.github.turansky.yfiles.ide.binding.ExistedContextProperty.Type.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder

private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"
private const val LABEL_CONTEXT: String = "yfiles.styles.ILabelTemplateStyleBindingContext"

internal val CONTEXT_PROPERTY_VARIANTS: Array<out Any> by lazy {
    sequenceOf<ExistedContextProperty>()
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

internal interface ExistedContextProperty : IContextProperty {
    val type: Type

    override val isStandard: Boolean
        get() = true

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

private enum class ContextProperty(
    override val type: Type
) : ExistedContextProperty {
    bounds(RECT),
    canvasComponent(CANVAS_COMPONENT),
    height(DOUBLE),
    item(IMODEL_ITEM),
    itemFocused(BOOLEAN),
    itemHighlighted(BOOLEAN),
    itemSelected(BOOLEAN),
    styleTag(STYLE_TAG),
    width(DOUBLE),
    zoom(DOUBLE);

    override val className = CONTEXT
}

private enum class LabelContextProperty(
    override val type: Type
) : ExistedContextProperty {
    isFlipped(BOOLEAN),
    isUpsideDown(BOOLEAN),
    labelText(STRING);

    override val className = LABEL_CONTEXT
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

private fun ExistedContextProperty.toVariant(): LookupElement =
    LookupElementBuilder.create(name)
        .withTypeText(type.toString(), true)
