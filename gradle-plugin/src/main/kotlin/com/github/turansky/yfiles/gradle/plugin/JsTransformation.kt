package com.github.turansky.yfiles.gradle.plugin

private val DESCRIPTOR_REGEX = Regex("(Object\\.defineProperty\\(.+\\.prototype, '.+', \\{)")

// TODO: Remove after fix https://youtrack.jetbrains.com/issue/KT-34770
internal fun String.fixPropertyObjectDescriptor(): String =
    replace(DESCRIPTOR_REGEX, "$1 configurable: true,")
