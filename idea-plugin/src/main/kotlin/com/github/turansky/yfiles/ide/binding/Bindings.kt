package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.template.TemplateFileType
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttributeValue

internal val XmlAttributeValue.bindingEnabled: Boolean
    get() = containingFile.bindingEnabled

private val PsiFile.bindingEnabled: Boolean
    get() = fileType.defaultExtension
        .equals(TemplateFileType.defaultExtension, ignoreCase = true)
