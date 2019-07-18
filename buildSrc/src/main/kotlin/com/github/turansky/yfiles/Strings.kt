package com.github.turansky.yfiles

fun between(str: String, start: String, end: String, firstEnd: Boolean = false): String {
    val startIndex = str.indexOf(start)
    require(startIndex != -1)
    { "String '$str' doesn't contain '$start'" }

    val endIndex = if (firstEnd) {
        str.indexOf(end)
    } else {
        str.lastIndexOf(end)
    }
    require(endIndex != -1)
    { "String '$str' doesn't contain '$end'" }

    return str.substring(startIndex + start.length, endIndex)
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