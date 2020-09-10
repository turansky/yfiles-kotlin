package com.github.turansky.yfiles.ide.psi

import com.intellij.psi.PsiElement

internal object DefaultPsiFinder : PsiFinder {
    private fun <T : Any> find(transform: (PsiFinder) -> T?): T? =
        PsiFinder.EP_NAME.extensionList.asSequence()
            .mapNotNull(transform)
            .firstOrNull()

    override fun findClass(
        context: PsiElement,
        className: String
    ): PsiElement? =
        find { it.findClass(context, className) }

    override fun findProperty(
        context: PsiElement,
        className: String,
        propertyName: String
    ): PsiElement? =
        find { it.findProperty(context, className, propertyName) }

    override fun findPropertyVariants(
        context: PsiElement,
        classNames: List<String>
    ): Array<out Any>? =
        find { it.findPropertyVariants(context, classNames) }
}
