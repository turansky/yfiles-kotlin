package com.github.turansky.yfiles.ide.color

import java.awt.Color

fun getHSLColor(
    h: Float,
    s: Float,
    l: Float,
    a: Float = 1f
): Color {
    check(s in 0f..100f)
    check(l in 0f..100f)
    check(a in 0f..1f)

    val hue = h % 360f / 360f
    val sp = s / 100f
    val lp = l / 100f

    val q = if (lp < 0.5) lp * (1 + sp) else lp + sp - sp * lp
    val p = 2 * lp - q

    return Color(
        toRGB(p, q, hue + 1f / 3f).coerceIn(0f, 1f),
        toRGB(p, q, hue).coerceIn(0f, 1f),
        toRGB(p, q, hue - 1f / 3f).coerceIn(0f, 1f),
        a
    )
}

private fun toRGB(
    p: Float,
    q: Float,
    h: Float
): Float {
    var hue = h
    if (hue < 0) hue += 1f
    if (hue > 1) hue -= 1f

    return when {
        6 * hue < 1
        -> p + (q - p) * 6 * hue

        2 * hue < 1
        -> q

        3 * hue < 2
        -> p + (q - p) * 6 * (2f / 3f - hue)

        else -> p
    }
}
