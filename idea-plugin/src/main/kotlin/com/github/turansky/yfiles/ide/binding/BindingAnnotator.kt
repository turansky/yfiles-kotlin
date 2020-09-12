package com.github.turansky.yfiles.ide.binding

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

            BindingParser.parse(block).forEach { (token, range) ->
                holder.info(BindingHighlightingColors[token], range(range))
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

private fun AnnotationHolder.comma(offset: Int) {
    info(BindingHighlightingColors.COMMA, TextRange.from(offset, 1))
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

