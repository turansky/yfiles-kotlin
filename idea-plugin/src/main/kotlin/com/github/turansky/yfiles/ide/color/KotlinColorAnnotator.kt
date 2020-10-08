package com.github.turansky.yfiles.ide.color

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.core.util.range
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty

private val COLOR_CLASS_NAMES = setOf(
    FqName("yfiles.view.Color"),
    FqName("yfiles.view.Fill"),
    FqName("yfiles.view.Stroke"),
)

internal class KotlinColorAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        when (element) {
            is KtLiteralStringTemplateEntry
            -> annotate(element, holder)

            is KtProperty
            -> annotate(element, holder)
        }
    }

    private fun annotate(
        element: KtLiteralStringTemplateEntry,
        holder: AnnotationHolder
    ) {
        val text = element.text ?: return
        val format = ColorFormat.values()
            .firstOrNull { it.matches(text) }
            ?: return

        holder.createColorAnnotation(text, format, element.range)
    }

    private fun annotate(
        element: KtProperty,
        holder: AnnotationHolder
    ) {
        val name = element.name ?: return

        if (element.isVar) return
        if (!element.isMember) return

        val companion = element.parent?.parent as? KtObjectDeclaration ?: return
        if (!companion.isCompanion()) return

        val klass = companion.parent?.parent as? KtClass ?: return
        if (klass.fqName !in COLOR_CLASS_NAMES) return

        val color = name.replace("_", "").toLowerCase()
        if (!ColorFormat.NAMED.matches(color)) return

        holder.createColorAnnotation(color, ColorFormat.NAMED, element.range)
    }
}
