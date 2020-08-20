package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.documentation.isSvgFile
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import org.jetbrains.kotlin.idea.highlighter.KotlinHighlightingColors

private val BINDING = Regex("\\s*(Binding|TemplateBinding)\\s*(.*)\\s*")
private val PARAMETER = Regex("\\s*(Converter|Parameter)\\s*(=)\\s*(.*)\\s*")

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
            localOffset += block.length + 1

            val bindingMatchResult = BINDING.find(block)
            if (bindingMatchResult != null) {
                val keywordGroup = bindingMatchResult.groups[1]!!
                val keywordRange = keywordGroup.range
                holder.keyword(offset + keywordRange.first, keywordRange.count())

                val dataGroup = bindingMatchResult.groups[2]!!
                val dataRange = dataGroup.range
                if (!dataRange.isEmpty()) {
                    val valid = BindingDirective.find(keywordGroup.value) != BindingDirective.TEMPLATE_BINDING
                    holder.parameter(offset + dataRange.first, dataRange.count(), valid)
                }
                continue
            }

            val parameterMatchResult = PARAMETER.find(block)!!

            val nameRange = parameterMatchResult.groups[1]!!.range
            holder.parameterName(offset + nameRange.first, nameRange.count())

            val assignRange = parameterMatchResult.groups[2]!!.range
            holder.assign(offset + assignRange.first)

            val dataRange = parameterMatchResult.groups[3]!!.range
            holder.parameter(offset + dataRange.first, dataRange.count())
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
