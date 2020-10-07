package com.github.turansky.yfiles.ide.color

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.xml.util.ColorIconCache
import com.intellij.xml.util.ColorMap
import org.jetbrains.kotlin.idea.core.util.range
import java.awt.Color
import javax.swing.Icon

internal class ColorAnnotator : Annotator {
    private val providers: List<ColorProvider> = listOf(
        KotlinColorProvider()
    )

    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        val data = providers.asSequence()
            .mapNotNull { it.get(element) }
            .firstOrNull()
            ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .gutterIconRenderer(IconRenderer(data.value))
            .range(element.range)
            .create()
    }
}

private class IconRenderer(
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
