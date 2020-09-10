package com.github.turansky.yfiles.ide.template

import com.intellij.ide.highlighter.XmlLikeFileType
import com.intellij.openapi.fileTypes.UIBasedFileType
import javax.swing.Icon

object TemplateFileType : XmlLikeFileType(TemplateLanguage), UIBasedFileType {
    override fun getName(): String = language.id
    override fun getDescription(): String = language.id
    override fun getDefaultExtension(): String = "svg"
    override fun getIcon(): Icon? = null
}
