package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.template.TemplateLanguage
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttributeValue

internal val XmlAttributeValue.bindingEnabled: Boolean
    get() = containingFile.bindingEnabled

private val PsiFile.bindingEnabled: Boolean
    get() = fileType.defaultExtension
        .equals(TemplateLanguage.defaultExtension, ignoreCase = true)
