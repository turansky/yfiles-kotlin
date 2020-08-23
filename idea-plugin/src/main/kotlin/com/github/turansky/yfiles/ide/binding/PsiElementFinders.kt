package com.github.turansky.yfiles.ide.binding

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFinder
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

internal fun findKotlinClass(
    context: PsiElement,
    className: String
): KtUltraLightClass? =
    PsiElementFinder.EP.getExtensions(context.project)
        .asSequence()
        .mapNotNull { it.findClass(className, context.resolveScope) }
        .firstOrNull() as? KtUltraLightClass

internal fun findKotlinProperty(
    context: PsiElement,
    className: String,
    propertyName: String
): PsiElement? {
    val klass = findKotlinClass(context, className)
        ?: return null

    return klass.kotlinOrigin.findPropertyByName(propertyName)
}
