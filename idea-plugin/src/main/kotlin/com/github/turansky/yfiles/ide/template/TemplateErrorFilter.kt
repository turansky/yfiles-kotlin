package com.github.turansky.yfiles.ide.template

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.psi.XmlPsiBundle

class TemplateErrorFilter : HighlightErrorFilter() {
    private val suppressedErrorDescription = XmlPsiBundle.message("xml.parsing.multiple.root.tags")

    override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean =
        !shouldSuppress(element)

    private fun shouldSuppress(element: PsiErrorElement): Boolean {
        val context = element.context as? XmlTag
            ?: return false

        return context.language == TemplateLanguage
                && element.errorDescription == suppressedErrorDescription
    }
}
