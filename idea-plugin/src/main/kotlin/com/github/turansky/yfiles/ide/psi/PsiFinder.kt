package com.github.turansky.yfiles.ide.psi

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement


interface PsiFinder {
    fun findClass(
        context: PsiElement,
        className: String,
    ): PsiElement?

    fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String,
    ): PsiElement?

    fun findPropertyVariants(
        context: PsiElement,
        classNames: List<String>,
    ): Array<out Any>? = null

    companion object {
        val EP_NAME: ExtensionPointName<PsiFinder> = ExtensionPointName.create("com.github.turansky.yfiles.psiFinder")
    }
}

