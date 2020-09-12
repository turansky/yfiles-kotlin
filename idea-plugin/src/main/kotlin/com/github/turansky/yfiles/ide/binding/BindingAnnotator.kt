package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.PARAMETER
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue

internal class BindingAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        if (element !is XmlAttributeValue) return
        if (!element.bindingEnabled) return

        val value = element.value
        value.toBinding() ?: return

        val valueRange = element.valueTextRange
        holder.languageFragment(valueRange)
        holder.brace(valueRange.startOffset)
        holder.brace(valueRange.endOffset - 1)

        val codeStartOffset = valueRange.startOffset + 1

        val code = value.drop(1).dropLast(1)
        val blocks = code.split(',')

        var localOffset = 0
        for (block in blocks) {
            val offset = codeStartOffset + localOffset
            val range = { source: IntRange -> TextRange.from(offset + source.first, source.count()) }

            val bindingMatchResult = BindingParser.KEYWORD.find(block)
            if (bindingMatchResult != null) {
                holder.keyword(range(bindingMatchResult.r(1)))

                val dataRange = bindingMatchResult.r(2)
                if (!dataRange.isEmpty()) {
                    holder.parameter(range(dataRange))
                }
            } else {
                val matchResult = BindingParser.ARGUMENT.find(block)
                if (matchResult != null) {
                    val valueMode = matchResult.d(1) == PARAMETER
                    holder.parameterName(range(matchResult.r(1)))
                    holder.assign(range(matchResult.r(2)))

                    holder.parameter(range(matchResult.r(3)), valueMode)
                } else {
                    holder.error(range(block.indices))
                }
            }

            localOffset += block.length + 1
            if (localOffset < code.length) {
                holder.comma(codeStartOffset + localOffset - 1)
            }
        }
    }
}

private fun AnnotationHolder.languageFragment(range: TextRange) {
    info(BindingHighlightingColors.LANGUAGE_INJECTION, range)
}

private fun AnnotationHolder.brace(offset: Int) {
    info(BindingHighlightingColors.BRACE, TextRange.from(offset, 1))
}

private fun AnnotationHolder.keyword(range: TextRange) {
    info(BindingHighlightingColors.KEYWORD, range)
}

private fun AnnotationHolder.parameterName(range: TextRange) {
    info(BindingHighlightingColors.NAMED_ARGUMENT, range)
}

private fun AnnotationHolder.assign(range: TextRange) {
    info(BindingHighlightingColors.ASSIGN, range)
}

private fun AnnotationHolder.comma(offset: Int) {
    info(BindingHighlightingColors.COMMA, TextRange.from(offset, 1))
}

private fun AnnotationHolder.parameter(range: TextRange, valueMode: Boolean = false) {
    info(if (valueMode) BindingHighlightingColors.VALUE else BindingHighlightingColors.ARGUMENT, range)
}

private fun AnnotationHolder.info(
    attributes: TextAttributesKey,
    range: TextRange
) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(attributes)
        .range(range)
        .create()
}

private fun AnnotationHolder.error(range: TextRange) {
    newSilentAnnotation(HighlightSeverity.ERROR)
        .textAttributes(BindingHighlightingColors.ERROR)
        .range(range)
        .create()
}
