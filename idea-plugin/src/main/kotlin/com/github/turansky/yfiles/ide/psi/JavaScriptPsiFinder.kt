package com.github.turansky.yfiles.ide.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement

class JavaScriptPsiFinder : PsiFinder {
    override fun findClass(
        context: PsiElement,
        className: String
    ): PsiClass? =
        null

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement? =
        null
}
