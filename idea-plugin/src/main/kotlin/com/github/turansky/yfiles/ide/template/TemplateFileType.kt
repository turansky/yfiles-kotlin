package com.github.turansky.yfiles.ide.template

import com.intellij.ide.highlighter.XmlLikeFileType
import com.intellij.openapi.fileTypes.UIBasedFileType
import javax.swing.Icon

class TemplateFileType private constructor() : XmlLikeFileType(TemplateLanguage), UIBasedFileType {
    override fun getName(): String = TemplateLanguage.id
    override fun getDescription(): String = TemplateLanguage.id
    override fun getDefaultExtension(): String = TemplateLanguage.defaultExtension
    override fun getIcon(): Icon? = null

    companion object {
        @JvmField
        val INSTANCE = TemplateFileType()
    }
}
