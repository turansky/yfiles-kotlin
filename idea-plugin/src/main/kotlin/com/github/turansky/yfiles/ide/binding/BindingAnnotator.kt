package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingToken.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
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
        holder.info(LANGUAGE_INJECTION, valueRange)
        holder.info(BRACE, valueRange.startOffset)
        holder.info(BRACE, valueRange.endOffset - 1)

        val codeStartOffset = valueRange.startOffset + 1

        val code = value.drop(1).dropLast(1)
        val blocks = code.split(',')

        var localOffset = 0
        for (block in blocks) {
            val offset = codeStartOffset + localOffset

            BindingParser.parse(block).forEach { (token, range) ->
                holder.info(token, TextRange.from(offset + range.first, range.count()))
            }

            localOffset += block.length + 1
            if (localOffset < code.length) {
                holder.info(COMMA, codeStartOffset + localOffset - 1)
            }
        }
    }
}

private fun AnnotationHolder.info(
    token: BindingToken,
    range: TextRange
) {
    val severity = when (token) {
        ERROR -> HighlightSeverity.ERROR
        else -> HighlightSeverity.INFORMATION
    }

    newSilentAnnotation(severity)
        .textAttributes(BindingHighlightingColors[token])
        .range(range)
        .create()
}

private fun AnnotationHolder.info(
    token: BindingToken,
    offset: Int
) {
    info(token, TextRange.from(offset, 1))
}

