package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.documentation.isSvgFile
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import org.jetbrains.kotlin.idea.highlighter.KotlinHighlightingColors

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
            .let { TextRange.create(it.startOffset + 1, it.endOffset - 1) }

        val directives = BindingDirective.values()
            .asSequence()
            .map { it.key }
            .filter { it in value }
            .toList()

        for (directive in directives) {
            val index = value.indexOf(directive)
            val range = TextRange.from(valueRange.startOffset + index, directive.length)

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(KotlinHighlightingColors.KEYWORD)
                .range(range)
                .create()
        }
    }
}
