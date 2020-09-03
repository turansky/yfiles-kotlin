package com.github.turansky.yfiles.ide.psi

import com.intellij.psi.PsiElement

internal object DefaultPsiFinder : PsiFinder {
    private val finders = listOf(
        KotlinPsiFinder(),
        JavaPsiFinder(),
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
