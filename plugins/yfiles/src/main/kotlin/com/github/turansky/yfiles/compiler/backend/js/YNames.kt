package com.github.turansky.yfiles.compiler.backend.js

private val DELIMITER = "$"

internal fun generateName(vararg names: String): String =
    names.joinToString(DELIMITER)
