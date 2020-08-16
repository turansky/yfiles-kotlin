package com.github.turansky.yfiles.ide.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue

internal class BindingDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement,
        originalElement: PsiElement?
    ): String? {
        val attributeValue = originalElement?.context as? XmlAttributeValue ?: return null
        if (!attributeValue.containingFile.isSvgFile) return null

        val value = attributeValue.value
        return if (value.isBindingLike) {
            "Template binding detected!"
        } else {
            null
        }
    }
}

private val String.isBindingLike: Boolean
    get() {
        if (!endsWith("}")) return false

        return startsWith("{TemplateBinding") || startsWith("{Binding")
    }
