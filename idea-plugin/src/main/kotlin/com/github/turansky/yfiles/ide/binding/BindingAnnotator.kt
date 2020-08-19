package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.documentation.isSvgFile
import com.github.turansky.yfiles.ide.documentation.toBinding
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
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

        element.value.toBinding() ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .textAttributes(KotlinHighlightingColors.SMART_CAST_VALUE)
            .range(element)
            .create()
    }
}
