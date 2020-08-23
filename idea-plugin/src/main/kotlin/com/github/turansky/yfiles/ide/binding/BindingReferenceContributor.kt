package com.github.turansky.yfiles.ide.binding

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.idea.core.util.range

internal class BindingReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(XmlAttributeValue::class.java),
            BindingReferenceProvider()
        )
    }
}

private class BindingReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<out PsiReference> {
        element as XmlAttributeValue

        val name = element.value.toBinding()
            ?.let { it as? TemplateBinding }
            ?.name
            ?: return PsiReference.EMPTY_ARRAY

        val valueOffset = element.valueTextRange.startOffset - element.range.startOffset
        val nameStartOffset = element.value.indexOf(name) + valueOffset
        val property = ContextProperty(
            element,
            TextRange.from(nameStartOffset, name.length),
            name
        )
        return arrayOf(property)
    }
}

private class ContextProperty(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    name: String
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, isContextParameter(name)) {
    override fun resolve(): PsiElement? {
        return null
    }
}
