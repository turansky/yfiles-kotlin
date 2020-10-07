package com.github.turansky.yfiles.ide.color

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.core.util.range
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry


private val RGB_PATTERN = Regex("#([A-Fa-f0-9]{6})")
private const val RGB_LENGTH = 7

internal class KotlinColorAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        if (element !is KtLiteralStringTemplateEntry) return
        val text = element.text ?: return

        if (text.length != RGB_LENGTH || !RGB_PATTERN.matches(text)) {
            return
        }

        holder.createColorAnnotation(text, element.range)
    }
}
