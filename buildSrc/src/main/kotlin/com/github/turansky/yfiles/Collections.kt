package com.github.turansky.yfiles

internal fun <T> Iterable<T>.byComma(transform: ((T) -> CharSequence)? = null): String =
    joinToString(separator = ", ", transform = transform)

internal fun <T> Iterable<T>.byCommaLine(transform: ((T) -> CharSequence)? = null): String =
    joinToString(separator = ",\n", transform = transform)

internal fun <T> Iterable<T>.lines(transform: ((T) -> CharSequence)? = null): String =
    joinToString(separator = "\n", postfix = "\n", transform = transform)
