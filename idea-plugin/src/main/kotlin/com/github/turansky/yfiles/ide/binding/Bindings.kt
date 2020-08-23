package com.github.turansky.yfiles.ide.binding

import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttributeValue

private const val SVG_EXTENSION = "svg"

internal val XmlAttributeValue.bindingEnabled: Boolean
    get() = containingFile.isSvgFile

private val PsiFile.isSvgFile: Boolean
    get() = fileType.defaultExtension.toLowerCase() == SVG_EXTENSION
