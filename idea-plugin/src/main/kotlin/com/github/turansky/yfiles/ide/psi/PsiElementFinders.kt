package com.github.turansky.yfiles.ide.psi

import com.intellij.psi.PsiElement

interface PsiFinder {
    fun findClass(
        context: PsiElement,
        className: String
    ): PsiElement?

    fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement?

    fun findPropertyVariants(
        context: PsiElement,
        classNames: List<String>
    ): Array<out Any>? = null
}

