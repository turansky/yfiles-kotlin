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
