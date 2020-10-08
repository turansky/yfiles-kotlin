package com.github.turansky.yfiles.ide.color

import com.intellij.xml.util.ColorMap
import java.awt.Color

enum class ColorFormat {
    RGB {
        private val PATTERN = Regex("#[A-Fa-f0-9]{6}")
        private val LENGTH = 7

        override fun matches(color: String): Boolean =
            color.length == LENGTH && PATTERN.matches(color)

        override fun parse(color: String): Color =
            ColorMap.getColor(color)
    };

    abstract fun matches(color: String): Boolean
    abstract fun parse(color: String): Color
}
