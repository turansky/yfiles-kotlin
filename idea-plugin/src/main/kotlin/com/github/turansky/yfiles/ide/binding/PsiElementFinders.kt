package com.github.turansky.yfiles.ide.binding

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.PsiShortNamesCache
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

sealed class PsiFinder {
    abstract fun findClass(
        context: PsiElement,
        className: String
    ): PsiElement?

    abstract fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement?
}

val DefaultPsiFinder: PsiFinder = KotlinPsiFinder

private object KotlinPsiFinder : PsiFinder() {
    override fun findClass(
        context: PsiElement,
        className: String
    ): KtClassOrObject? =
        KotlinFullClassNameIndex.getInstance()
            .get(className, context.project, context.resolveScope)
            .firstOrNull()

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement? =
        findClass(context, className)
            ?.findPropertyByName(propertyName)
}

private object JavaPsiFinder : PsiFinder() {
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

private object JavaScriptPsiFinder : PsiFinder() {
    override fun findClass(
        context: PsiElement,
        className: String
    ): PsiClass? =
        PsiShortNamesCache.getInstance(context.project)
            .getClassesByName(className.substringAfterLast("."), context.resolveScope)
            .firstOrNull()

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement? =
        findClass(context, className)
            ?.findFieldByName(propertyName, true)
}
