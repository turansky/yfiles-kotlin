package com.github.turansky.yfiles.compiler.extensions

private val DELIMITER = "$"

internal fun generateName(vararg names: String): String =
    names.joinToString(DELIMITER)
