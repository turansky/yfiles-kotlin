package com.github.turansky.yfiles.ide.psi

import com.intellij.lang.javascript.index.JavaScriptIndex
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement

class JavaScriptPsiFinder : PsiFinder {
    override fun findClass(
        context: PsiElement,
        className: String
    ): JSClass? =
        JavaScriptIndex.getInstance(context.project)
            .getClassByName(className.substringAfterLast("."), true)
            .filterIsInstance<JSClass>()
            .firstOrNull()

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement? =
        findClass(context, className)
            ?.findFieldByName(propertyName)
}
