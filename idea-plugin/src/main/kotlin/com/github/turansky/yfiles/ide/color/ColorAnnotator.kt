package com.github.turansky.yfiles.ide.color

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.xml.util.ColorIconCache
import org.jetbrains.kotlin.idea.core.util.range
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import java.awt.Color
import javax.swing.Icon


private val RGB_PATTERN = Regex("#([A-Fa-f0-9]{6})")
private const val RGB_LENGTH = 7

internal class ColorAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        if (element !is KtLiteralStringTemplateEntry) return
        val text = element.text ?: return

        if (text.length != RGB_LENGTH || !RGB_PATTERN.matches(text)) {
            return
        }

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .gutterIconRenderer(IconRenderer(text))
            .range(element.range)
            .create()
    }
}

private class IconRenderer(
    private val colorText: String
) : GutterIconRenderer() {
    private val color: Color
        get() {
            val code = colorText.substring(1).toInt(16)
            return Color(
                code shr 16 and 0xFF,
                code shr 8 and 0xFF,
                code and 0xFF
            )
        }

    override fun getIcon(): Icon =
        ColorIconCache.getIconCache()
            .getIcon(color, ICON_SIZE)

    override fun equals(other: Any?): Boolean =
        this === other

    override fun hashCode(): Int =
        colorText.hashCode()

    companion object {
        private const val ICON_SIZE = 10
    }
}
