package com.github.turansky.yfiles.ide.binding

internal fun join(
    first: String,
    delimiter: String,
    second: String?
): String =
    if (second != null) {
        "$first$delimiter$second"
    } else {
        first
    }


internal fun String.trimBraces(): String? =
    when {
        !startsWith("{") -> null
        !endsWith("}") -> null
        else -> removePrefix("{").removeSuffix("}").trim().takeIf { it.isNotEmpty() }
    }
