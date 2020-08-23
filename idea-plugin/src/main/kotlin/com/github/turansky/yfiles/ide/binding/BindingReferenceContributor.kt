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

        val binding = element.value.toBinding()
            ?: return PsiReference.EMPTY_ARRAY

        if (binding !is TemplateBinding)
            return PsiReference.EMPTY_ARRAY

        val name = binding.name ?: return PsiReference.EMPTY_ARRAY

        val nameStartOffset = element.value.indexOf(name) + element.valueTextRange.startOffset - element.range.startOffset
        return arrayOf(ContextProperty(element, TextRange.from(nameStartOffset, name.length)))
    }
}

private class ContextProperty(
    element: XmlAttributeValue,
    rangeInElement: TextRange
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement) {
    override fun resolve(): PsiElement? {
        val nameRange = rangeInElement.shiftRight(element.range.startOffset).shiftLeft(element.valueTextRange.startOffset)
        val name = element.value.substring(nameRange.startOffset, nameRange.endOffset)
        return if (isContextParameter(name)) element else null
    }
}
