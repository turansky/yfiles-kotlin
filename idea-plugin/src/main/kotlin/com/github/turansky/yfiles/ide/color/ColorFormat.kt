package com.github.turansky.yfiles.ide.color

import com.intellij.xml.util.ColorMap
import java.awt.Color

enum class ColorFormat {
    SHORT_HEX {
        private val PATTERN = Regex("#[A-Fa-f0-9]{3}")
        private val LENGTH = 4

        override fun matches(color: String): Boolean =
            color.length == LENGTH && PATTERN.matches(color)

        override fun parse(color: String): Color {
            val code = color.substring(1).toInt(16)
            return Color(
                (code shr 8 and 15) * 17,
                (code shr 4 and 15) * 17,
                (code and 15) * 17
            )
        }
    },

    HEX {
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
