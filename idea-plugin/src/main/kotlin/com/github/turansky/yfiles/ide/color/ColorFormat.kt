package com.github.turansky.yfiles.ide.color

import com.intellij.xml.util.ColorMap
import java.awt.Color

enum class ColorFormat {
    NAMED {
        override fun matches(color: String): Boolean =
            ColorMap.getHexCodeForColorName(color) != null

        override fun parse(color: String): Color {
            val code = ColorMap.getHexCodeForColorName(color)!!
            return ColorMap.getColor(code)!!
        }
    },

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
    },

    RGB {
        private val PATTERN = Regex("rgb\\((\\d{1,3}), (\\d{1,3}), (\\d{1,3})\\)")
        private val LENGTH_RANGE = 12..18

        override fun matches(color: String): Boolean =
            color.length in LENGTH_RANGE && PATTERN.matches(color)

        override fun parse(color: String): Color {
            val (r, g, b) = PATTERN.find(color)!!
                .groupValues
                .asSequence()
                .drop(1)
                .map { it.toInt() }
                .map { it and 255 }
                .toList()

            return Color(r, g, b)
        }
    },

    HSL {
        private val PATTERN = Regex("hsl\\((\\d{1,3}), (\\d{1,3})%, (\\d{1,3})%\\)")
        private val LENGTH_RANGE = 14..20

        override fun matches(color: String): Boolean =
            color.length in LENGTH_RANGE && PATTERN.matches(color)

        override fun parse(color: String): Color {
            val (h, s, l) = PATTERN.find(color)!!
                .groupValues
                .asSequence()
                .drop(1)
                .map { it.toFloat() }
                .toList()

            return getHSLColor(
                h = h,
                s = minOf(s, 100f),
                l = minOf(l, 100f)
            )
        }
    };

    abstract fun matches(color: String): Boolean
    abstract fun parse(color: String): Color
}
