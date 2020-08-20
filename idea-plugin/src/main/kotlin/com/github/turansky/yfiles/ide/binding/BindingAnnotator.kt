package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.documentation.isSvgFile
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import org.jetbrains.kotlin.idea.highlighter.KotlinHighlightingColors

private val BINDING = Regex("\\s*(Binding|TemplateBinding)\\s*(\\S*)\\s*")
private val PARAMETER = Regex("\\s*(Converter|Parameter)\\s*(=)\\s*(\\S*)\\s*")

internal class BindingAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        if (element !is XmlAttributeValue) return
        if (!element.containingFile.isSvgFile) return

        val value = element.value
        value.toBinding() ?: return

        val codeStartOffset = element.textRange.startOffset + 2

        val code = value.drop(1).dropLast(1)
        val blocks = code.split(',')

        var localOffset = 0
        for (block in blocks) {
            val offset = codeStartOffset + localOffset

            val bindingMatchResult = BINDING.find(block)
            if (bindingMatchResult != null) {
                val keywordGroup = bindingMatchResult.g(1)
                val keywordRange = keywordGroup.range
                holder.keyword(offset + keywordRange.first, keywordRange.count())

                val dataGroup = bindingMatchResult.g(2)
                val dataRange = dataGroup.range
                if (!dataRange.isEmpty()) {
                    val valid = when (BindingDirective.find(keywordGroup.value)) {
                        BindingDirective.TEMPLATE_BINDING -> isContextParameter(dataGroup.value)
                        else -> true
                    }
                    holder.parameter(offset + dataRange.first, dataRange.count(), valid)
                }
            } else {
                val parameterMatchResult = PARAMETER.find(block)!!

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

private fun AnnotationHolder.keyword(offset: Int, length: Int) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(KotlinHighlightingColors.KEYWORD)
        .range(TextRange.from(offset, length))
        .create()
}

private fun AnnotationHolder.parameterName(offset: Int, length: Int) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(KotlinHighlightingColors.NAMED_ARGUMENT)
        .range(TextRange.from(offset, length))
        .create()
}

private fun AnnotationHolder.assign(offset: Int) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(KotlinHighlightingColors.NAMED_ARGUMENT)
        .range(TextRange.from(offset, 1))
        .create()
}

private fun AnnotationHolder.comma(offset: Int) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(KotlinHighlightingColors.COMMA)
        .range(TextRange.from(offset, 1))
        .create()
}

private fun AnnotationHolder.parameter(offset: Int, length: Int, valid: Boolean = true) {
    val textAttributes = if (valid) {
        KotlinHighlightingColors.PARAMETER
    } else {
        KotlinHighlightingColors.BAD_CHARACTER
    }

    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(textAttributes)
        .range(TextRange.from(offset, length))
        .create()
}
