package com.github.turansky.yfiles.ide.color

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry

private val RGB_PATTERN = Regex("#([A-Fa-f0-9]{6})")
private const val RGB_LENGTH = 7

class KotlinColorProvider : ColorProvider {
    override fun get(element: PsiElement): ColorData? {
        if (element !is KtLiteralStringTemplateEntry) return null
        val text = element.text ?: return null

        if (text.length != RGB_LENGTH || !RGB_PATTERN.matches(text)) {
            return null
        }

        return ColorData(text)
    }
}
