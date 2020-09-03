package com.github.turansky.yfiles.ide.binding

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

internal sealed class PsiFinder {
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

internal object DefaultPsiFinder : PsiFinder() {
    private val finders = listOf(
        KotlinPsiFinder,
        JavaPsiFinder,
    )

    override fun findClass(
        context: PsiElement,
        className: String
    ): PsiElement? =
        finders.asSequence()
            .mapNotNull { it.findClass(context, className) }
            .firstOrNull()

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement? =
        finders.asSequence()
            .mapNotNull { it.findProperty(context, className, propertyName) }
            .firstOrNull()

    override fun findPropertyVariants(
        context: PsiElement,
        classNames: List<String>
    ): Array<out Any>? =
        finders.asSequence()
            .mapNotNull { it.findPropertyVariants(context, classNames) }
            .firstOrNull()
}

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

    override fun findPropertyVariants(
        context: PsiElement,
        classNames: List<String>
    ): Array<out Any>? {
        val classes = classNames
            .mapNotNull { findClass(context, it) }
            .takeIf { it.isNotEmpty() }
            ?: return null

        return classes
            .asSequence()
            .flatMap { it.declarations }
            .filterIsInstance<KtProperty>()
            .map { it.toVariant() }
            .toList()
            .toTypedArray()
    }

    private fun KtProperty.toVariant(): LookupElement =
        LookupElementBuilder.create(this)
            .withIcon(AllIcons.Nodes.PropertyRead)
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
