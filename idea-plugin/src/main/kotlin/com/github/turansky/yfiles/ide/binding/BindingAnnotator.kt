package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingToken.ERROR
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

        val offset = element.valueTextRange.startOffset
        BindingParser.parse(value).forEach { (token, range) ->
            holder.info(token, TextRange.from(offset + range.first, range.count()))
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

