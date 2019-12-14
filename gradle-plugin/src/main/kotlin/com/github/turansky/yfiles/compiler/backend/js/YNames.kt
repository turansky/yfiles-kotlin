package com.github.turansky.yfiles.compiler.backend.js

import org.jetbrains.kotlin.descriptors.ClassDescriptor

private val DELIMITER = "$"

internal fun generateName(vararg names: String): String =
    names.joinToString(DELIMITER)

internal fun generateName(
    descriptor: ClassDescriptor,
    additionalName: String
): String =
    generateName(descriptor.name.identifier, additionalName)
