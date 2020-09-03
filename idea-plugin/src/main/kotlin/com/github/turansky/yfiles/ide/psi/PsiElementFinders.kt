package com.github.turansky.yfiles.ide.psi

import com.intellij.psi.PsiElement

abstract class PsiFinder {
    abstract fun findClass(
        context: PsiElement,
        className: String
    ): PsiElement?

    abstract fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement?

    open fun findPropertyVariants(
        context: PsiElement,
        classNames: List<String>
    ): Array<out Any>? = null
}

