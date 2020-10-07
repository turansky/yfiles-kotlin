package com.github.turansky.yfiles.ide.color

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.xml.util.ColorIconCache
import com.intellij.xml.util.ColorMap
import java.awt.Color
import javax.swing.Icon

internal fun AnnotationHolder.createColorAnnotation(
    colorText: String,
    range: TextRange
) {
    newSilentAnnotation(HighlightSeverity.INFORMATION)
        .gutterIconRenderer(ColorIconRenderer(colorText))
        .range(range)
        .create()
}

private class ColorIconRenderer(
    private val colorText: String
) : GutterIconRenderer() {
    private val color: Color
        get() = ColorMap.getColor(colorText)

    override fun getIcon(): Icon =
        ColorIconCache.getIconCache()
            .getIcon(color, ICON_SIZE)

    override fun equals(other: Any?): Boolean =
        this === other

    override fun hashCode(): Int =
        colorText.hashCode()

    companion object {
        private const val ICON_SIZE = 8
    }
}
