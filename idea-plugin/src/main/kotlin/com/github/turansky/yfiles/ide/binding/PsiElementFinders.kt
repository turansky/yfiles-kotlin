package com.github.turansky.yfiles.ide.binding

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFinder
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

internal fun findKotlinProperty(
    context: PsiElement,
    className: String,
    propertyName: String
): PsiElement? {
    val psiClass = PsiElementFinder.EP.getExtensions(context.project)
        .asSequence()
        .mapNotNull { it.findClass(className, context.resolveScope) }
        .firstOrNull()
        ?: return null

    return when (psiClass) {
        is KtUltraLightClass -> psiClass.kotlinOrigin.findPropertyByName(propertyName)
        else -> null
    }
}
