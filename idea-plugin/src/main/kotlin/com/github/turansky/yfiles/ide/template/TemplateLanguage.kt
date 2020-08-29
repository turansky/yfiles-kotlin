package com.github.turansky.yfiles.ide.template

import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.xml.XMLParserDefinition
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.xml.XmlFileImpl
import com.intellij.psi.tree.IFileElementType

internal object TemplateLanguage : XMLLanguage(
    INSTANCE,
    "yfiles-template",
    "text/yfiles-template"
) {
    const val defaultExtension: String = "svg"
}

class TemplateParserDefinition : XMLParserDefinition() {
    override fun getFileNodeType(): IFileElementType =
        TEMPLATE_FILE

    override fun createFile(viewProvider: FileViewProvider): PsiFile =
        XmlFileImpl(viewProvider, TEMPLATE_FILE)

    private companion object {
        val TEMPLATE_FILE = IFileElementType(TemplateLanguage)
    }
}
