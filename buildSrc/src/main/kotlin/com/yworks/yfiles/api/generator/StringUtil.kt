package com.yworks.yfiles.api.generator

import org.gradle.api.GradleException

fun between(str: String, start: String, end: String, firstEnd: Boolean = false): String {
    val startIndex = str.indexOf(start)
    if (startIndex == -1) {
        throw GradleException("String '$str' doesn't contains '$start'")
    }

    val endIndex = if (firstEnd) {
        str.indexOf(end)
    } else {
        str.lastIndexOf(end)
    }
    if (endIndex == -1) {
        throw GradleException("String '$str' doesn't contains '$end'")
    }

    return str.substring(startIndex + start.length, endIndex)
}

fun hardBetween(str: String, start: String, end: String): String {
    if (!str.startsWith(start)) {
        throw GradleException("String '$str' not started from '$start'")
    }

    if (!str.endsWith(end)) {
        throw GradleException("String '$str' not ended with '$end'")
    }

    return str.substring(start.length, str.length - end.length)
}

fun till(str: String, end: String): String {
    val endIndex = str.indexOf(end)
    if (endIndex == -1) {
        throw GradleException("String '$str' doesn't contains '$end'")
    }

    return str.substring(0, endIndex)
}

fun from(str: String, start: String): String {
    val startIndex = str.lastIndexOf(start)
    if (startIndex == -1) {
        throw GradleException("String '$str' doesn't contains '$start'")
    }

    return str.substring(startIndex + start.length)
}

fun from2(str: String, start: String): String {
    val startIndex = str.indexOf(start)
    if (startIndex == -1) {
        throw GradleException("String '$str' doesn't contains '$start'")
    }

    return str.substring(startIndex + start.length)
}
