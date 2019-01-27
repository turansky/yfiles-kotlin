package com.yworks.yfiles.api.generator

internal fun <T> Iterable<T>.byComma(transform: ((T) -> CharSequence)? = null): String {
    return joinToString(separator = ", ", transform = transform)
}

internal fun <T> Sequence<T>.byComma(transform: ((T) -> CharSequence)? = null): String {
    return joinToString(separator = ", ", transform = transform)
}