package com.github.turansky.yfiles.ide.psi

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement

class JavaPsiFinder : PsiFinder {
    override fun findClass(
        context: PsiElement,
        className: String
    ): PsiClass? =
        JavaPsiFacade.getInstance(context.project)
            .findClass("com.yworks.$className", context.resolveScope)

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement? =
        findClass(context, className)
            ?.findMethodsByName(propertyName.toMethodName(), true)
            ?.firstOrNull()

    private fun String.toMethodName(): String =
        when {
            startsWith("is") -> this
            endsWith("ed") -> "is" + capitalize()
            else -> "get" + capitalize()
        }
}
