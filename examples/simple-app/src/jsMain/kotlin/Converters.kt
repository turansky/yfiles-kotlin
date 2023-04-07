@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.styles.Templates
import yfiles.styles.invoke
import yfiles.styles.put

fun converters() {
    Templates.CONVERTERS {
        put("visibility", ::visibility)
        put("color", ::color)
    }
}

fun visibility(visible: Boolean): String {
    return if (visible) "visible" else "none"
}

fun color(width: Double, defaultColor: String): String =
    when {
        width > 10 -> "white"
        width > 20 -> "grey"
        else -> defaultColor
    }
