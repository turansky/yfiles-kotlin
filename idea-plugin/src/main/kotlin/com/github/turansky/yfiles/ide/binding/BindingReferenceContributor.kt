package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.TEMPLATE_BINDING
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

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

        if (!element.bindingEnabled)
            return PsiReference.EMPTY_ARRAY

        val binding = element.value.toBinding() as? TemplateBinding
            ?: return PsiReference.EMPTY_ARRAY

        val value = element.value
        val valueOffset = element.valueTextRange.startOffset - element.textRange.startOffset

        val key = TEMPLATE_BINDING.key
        val keyIndex = value.indexOf(key)
        val keyStartOffset = keyIndex + valueOffset

        val classReference = ContextClassReference(
            element = element,
            rangeInElement = TextRange.from(keyStartOffset, key.length),
            className = binding.parentReference
        )

        val name = binding.name
            ?: return arrayOf(classReference)

        val nameStartOffset = value.indexOf(name, keyIndex + key.length) + valueOffset

        val propertyReference = ContextPropertyReference(
            element = element,
            rangeInElement = TextRange.from(nameStartOffset, name.length),
            property = binding.property
        )
        return arrayOf(
            classReference,
            propertyReference
        )
    }
}

private class ContextClassReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val className: String
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, true) {
    override fun resolve(): PsiElement? =
        DefaultPsiFinder.findClass(element, className)
}

private class ContextPropertyReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val property: IContextProperty
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, property.isStandard) {
    override fun resolve(): PsiElement? =
        if (property.isStandard) {
            DefaultPsiFinder.findProperty(element, property.className, property.name)
        } else {
            null
        }

    override fun getVariants(): Array<out Any> =
        CONTEXT_PROPERTY_VARIANTS
}
