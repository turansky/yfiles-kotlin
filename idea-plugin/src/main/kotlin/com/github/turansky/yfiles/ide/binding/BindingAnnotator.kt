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

internal class BindingAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        if (element !is XmlAttributeValue) return
        if (!element.containingFile.isSvgFile) return

        val value = element.value
        val code = value.trimBraces() ?: return
        value.toBinding() ?: return

        val codeStartOffset = element.textRange.startOffset + 2

        val blocks = code.split(',')

        var localOffset = 0
        for (block in blocks) {
            val offset = codeStartOffset + localOffset

            BINDING.find(block)?.also {
                val keywordRange = it.groups[1]!!.range
                holder.keyword(offset + keywordRange.first, keywordRange.count())

                val dataRange = it.groups[2]!!.range
                if (!dataRange.isEmpty()) {
                    holder.parameter(offset + dataRange.first, dataRange.count())
                }
            }

            localOffset += block.length + 1
        }
    }
}

private fun AnnotationHolder.keyword(offset: Int, length: Int) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(KotlinHighlightingColors.KEYWORD)
        .range(TextRange.from(offset, length))
        .create()
}

private fun AnnotationHolder.parameter(offset: Int, length: Int) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(KotlinHighlightingColors.PARAMETER)
        .range(TextRange.from(offset, length))
        .create()
}
