package com.github.turansky.yfiles.ide.color

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.core.util.range
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry

internal class KotlinColorAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        if (element !is KtLiteralStringTemplateEntry) return
        val text = element.text ?: return
        val format = ColorFormat.values()
            .firstOrNull { it.matches(text) }
            ?: return

        holder.createColorAnnotation(text, format, element.range)
    }
}
