package com.github.turansky.yfiles.ide.binding

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

internal fun findKotlinClass(
    context: PsiElement,
    className: String
): KtClassOrObject? =
    KotlinFullClassNameIndex.getInstance()
        .get(className, context.project, context.resolveScope)
        .firstOrNull()

internal fun findKotlinProperty(
    context: PsiElement,
    className: String,
    propertyName: String
): PsiElement? =
    findKotlinClass(context, className)
        ?.findPropertyByName(propertyName)
