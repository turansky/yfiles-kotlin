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
    private val className: String,
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, true) {
    override fun resolve(): PsiElement? =
        DefaultPsiFinder.findClass(element, className)
}

internal open class PropertyReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val property: IProperty,
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, true) {
    override fun resolve(): PsiElement? =
        DefaultPsiFinder.findProperty(element, property.className, property.name)
}

internal fun ContextPropertyReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    propertyName: String,
): PsiReference {
    val property = when {
        "." !in propertyName -> null
        ".." in propertyName -> null
        propertyName.endsWith(".") -> null
        else -> ContextProperty.findComplex(propertyName.substringBefore("."))
    }

    if (property != null) {
        return ContextPropertyReference(
            element = element,
            rangeInElement = TextRange.from(rangeInElement.startOffset, property.name.length),
            property = property
        )
    }

    return ContextPropertyReference(
        element = element,
        rangeInElement = rangeInElement,
        property = ContextProperty.find(propertyName)
    )
}

private class ContextPropertyReference(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val property: IProperty?,
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, property != null) {
    override fun resolve(): PsiElement? =
        if (property != null) {
            DefaultPsiFinder.findProperty(element, property.className, property.name)
        } else {
            null
        }

    override fun getVariants(): Array<out Any> =
        DefaultPsiFinder.findPropertyVariants(element, CONTEXT_CLASSES)
            ?: CONTEXT_PROPERTY_VARIANTS
}
