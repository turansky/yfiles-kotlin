package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*
import com.github.turansky.yfiles.ide.documentation.isSvgFile
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue

private val KEYWORD = Regex("\\s*($BINDING|$TEMPLATE_BINDING)\\s*(\\S*)\\s*")
private val ARGUMENT = Regex("\\s*($CONVERTER|$PARAMETER)\\s*(=)\\s*(\\S*)\\s*")

internal class BindingAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        if (element !is XmlAttributeValue) return
        if (!element.containingFile.isSvgFile) return

        val value = element.value
        value.toBinding() ?: return

        val valueRange = element.textRange
        holder.languageFragment(TextRange.create(valueRange.startOffset + 1, valueRange.endOffset - 1))

        val codeStartOffset = valueRange.startOffset + 2

        val code = value.drop(1).dropLast(1)
        val blocks = code.split(',')

        var localOffset = 0
        for (block in blocks) {
            val offset = codeStartOffset + localOffset
            val range = { source: IntRange -> TextRange.from(offset + source.first, source.count()) }

            val bindingMatchResult = KEYWORD.find(block)
            if (bindingMatchResult != null) {
                val keywordGroup = bindingMatchResult.g(1)
                holder.keyword(range(keywordGroup.range))

                val dataGroup = bindingMatchResult.g(2)
                val dataRange = dataGroup.range
                if (!dataRange.isEmpty()) {
                    val valid = when (BindingDirective.find(keywordGroup.value)) {
                        TEMPLATE_BINDING -> isContextParameter(dataGroup.value)
                        else -> true
                    }
                    holder.parameter(range(dataRange), valid)
                }
            } else {
                val matchResult = ARGUMENT.find(block)!!
                holder.parameterName(range(matchResult.r(1)))
                holder.assign(range(matchResult.r(2)))
                holder.parameter(range(matchResult.r(3)))
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

private fun AnnotationHolder.parameter(range: TextRange, valid: Boolean = true) {
    info(BindingHighlightingColors.ARGUMENT, range)

    if (valid) return

    newSilentAnnotation(HighlightSeverity.ERROR)
        .textAttributes(BindingHighlightingColors.RESOLVED_TO_ERROR)
        .range(range)
        .create()
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
