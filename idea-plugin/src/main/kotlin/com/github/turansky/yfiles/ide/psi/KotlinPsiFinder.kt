package com.github.turansky.yfiles.ide.psi

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

class KotlinPsiFinder : PsiFinder {
    override fun findClass(
        context: PsiElement,
        className: String,
    ): KtClassOrObject? =
        KotlinFullClassNameIndex.getInstance()
            .get(className, context.project, context.resolveScope)
            .firstOrNull()

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String,
    ): PsiElement? =
        findClass(context, className)
            ?.findPropertyByName(propertyName)

    override fun findPropertyVariants(
        context: PsiElement,
        classNames: List<String>,
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
