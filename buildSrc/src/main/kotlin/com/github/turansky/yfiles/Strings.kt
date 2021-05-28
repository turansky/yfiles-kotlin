package com.github.turansky.yfiles

internal fun String.between(
    start: String,
    end: String,
    firstEnd: Boolean = false,
): String {
    val startIndex = indexOf(start)
    require(startIndex != -1)
    { "String '$this' doesn't contain '$start'" }

    val endIndex = if (firstEnd) {
        indexOf(end)
    } else {
        lastIndexOf(end)
    }
    require(endIndex != -1)
    { "String '$this' doesn't contain '$end'" }

    return substring(startIndex + start.length, endIndex)
}

fun till(str: String, end: String): String {
    val endIndex = str.indexOf(end)
    require(endIndex != -1)
    { "String '$str' doesn't contain '$end'" }

    return str.substring(0, endIndex)
}

@Suppress("NOTHING_TO_INLINE")
inline fun exp(condition: Boolean, str: String): String {
    return if (condition) str else ""
}
