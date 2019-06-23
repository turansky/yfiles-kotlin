package com.github.turansky.yfiles

internal fun <T> Iterable<T>.byComma(transform: ((T) -> CharSequence)? = null): String {
    return joinToString(separator = ", ", transform = transform)
}

internal fun <T> Iterable<T>.byCommaLine(transform: ((T) -> CharSequence)? = null): String {
    return joinToString(separator = ",\n", transform = transform)
}

internal fun <T> Iterable<T>.lines(transform: ((T) -> CharSequence)? = null): String {
    return joinToString(separator = "\n", postfix = "\n", transform = transform)
}

internal fun <T> Sequence<T>.lines(transform: ((T) -> CharSequence)? = null): String {
    return joinToString(separator = "\n", postfix = "\n", transform = transform)
}