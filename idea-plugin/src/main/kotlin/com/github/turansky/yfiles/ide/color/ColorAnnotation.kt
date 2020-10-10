package com.github.turansky.yfiles.ide.color

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.xml.util.ColorIconCache
import javax.swing.Icon

internal fun AnnotationHolder.createColorAnnotation(
    colorText: String,
    format: ColorFormat,
    range: TextRange
) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .textAttributes(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
        .gutterIconRenderer(ColorIconRenderer(colorText, format))
        .range(range)
        .create()
}

private class ColorIconRenderer(
    private val colorText: String,
    private val format: ColorFormat
) : GutterIconRenderer() {
    override fun getIcon(): Icon =
        ColorIconCache.getIconCache()
            .getIcon(format.parse(colorText), ICON_SIZE)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColorIconRenderer

        if (colorText != other.colorText) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int =
        31 + colorText.hashCode() + format.hashCode()

    companion object {
        private const val ICON_SIZE = 8
    }
}
