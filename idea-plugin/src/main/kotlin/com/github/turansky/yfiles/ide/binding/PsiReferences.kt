package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.psi.DefaultPsiFinder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.xml.XmlAttributeValue

internal class ClassReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val className: String
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, true) {
    override fun resolve(): PsiElement? =
        DefaultPsiFinder.findClass(element, className)
}

internal open class PropertyReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val property: IProperty
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, property.isStandard) {
    override fun resolve(): PsiElement? =
        if (property.isStandard) {
            DefaultPsiFinder.findProperty(element, property.className, property.name)
        } else {
            null
        }
}

internal fun ContextPropertyReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    propertyName: String
): PsiReference {
    if ("." in propertyName) {
        val name = propertyName.substringBefore(".")
        val property = findContextProperty(name)
        if (property.isStandard) {
            return ContextPropertyReference(
                element = element,
                rangeInElement = TextRange.from(rangeInElement.startOffset, name.length),
                property = property
            )
        }
    }

    return ContextPropertyReference(
        element = element,
        rangeInElement = rangeInElement,
        property = findContextProperty(propertyName)
    )
}

private class ContextPropertyReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    property: IProperty
) : PropertyReference(element, rangeInElement, property) {
    override fun getVariants(): Array<out Any> =
        DefaultPsiFinder.findPropertyVariants(element, CONTEXT_CLASSES)
            ?: CONTEXT_PROPERTY_VARIANTS
}
