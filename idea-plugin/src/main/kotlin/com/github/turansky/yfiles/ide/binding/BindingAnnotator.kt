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
        holder.languageFragment(valueRange)

        val codeStartOffset = valueRange.startOffset + 2

        val code = value.drop(1).dropLast(1)
        val blocks = code.split(',')

        var localOffset = 0
        for (block in blocks) {
            val offset = codeStartOffset + localOffset

            val bindingMatchResult = KEYWORD.find(block)
            if (bindingMatchResult != null) {
                val keywordGroup = bindingMatchResult.g(1)
                val keywordRange = keywordGroup.range
                holder.keyword(offset + keywordRange.first, keywordRange.count())

                val dataGroup = bindingMatchResult.g(2)
                val dataRange = dataGroup.range
                if (!dataRange.isEmpty()) {
                    val valid = when (BindingDirective.find(keywordGroup.value)) {
                        TEMPLATE_BINDING -> isContextParameter(dataGroup.value)
                        else -> true
                    }
                    holder.parameter(offset + dataRange.first, dataRange.count(), valid)
                }
            } else {
                val parameterMatchResult = ARGUMENT.find(block)!!

                val nameRange = parameterMatchResult.r(1)
                holder.parameterName(offset + nameRange.first, nameRange.count())

                val assignRange = parameterMatchResult.r(2)
                holder.assign(offset + assignRange.first)

                val dataRange = parameterMatchResult.r(3)
                holder.parameter(offset + dataRange.first, dataRange.count())
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

private fun AnnotationHolder.keyword(offset: Int, length: Int) {
    info(BindingHighlightingColors.KEYWORD, TextRange.from(offset, length))
}

private fun AnnotationHolder.parameterName(offset: Int, length: Int) {
    info(BindingHighlightingColors.NAMED_ARGUMENT, TextRange.from(offset, length))
}

private fun AnnotationHolder.assign(offset: Int) {
    info(BindingHighlightingColors.ASSIGN, TextRange.from(offset, 1))
}

private fun AnnotationHolder.comma(offset: Int) {
    info(BindingHighlightingColors.COMMA, TextRange.from(offset, 1))
}

private fun AnnotationHolder.parameter(offset: Int, length: Int, valid: Boolean = true) {
    val range = TextRange.from(offset, length)
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
