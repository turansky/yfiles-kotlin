package com.github.turansky.yfiles.ide.documentation

import com.github.turansky.yfiles.ide.binding.bindingEnabled
import com.github.turansky.yfiles.ide.binding.toBinding
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue

internal class BindingDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement,
        originalElement: PsiElement?,
    ): String? {
        val attributeValue = originalElement?.context as? XmlAttributeValue ?: return null
        if (!attributeValue.bindingEnabled) return null

        return attributeValue.value.toBinding()?.let(::documentation)
    }
}
