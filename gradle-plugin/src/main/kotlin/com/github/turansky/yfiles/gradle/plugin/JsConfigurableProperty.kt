// TODO: Remove after fix https://youtrack.jetbrains.com/issue/KT-34770
package com.github.turansky.yfiles.gradle.plugin

private val DESCRIPTOR_REGEX = Regex("(Object\\.defineProperty\\(.+\\.prototype, '[a-zA-Z]+', \\{)")

internal fun String.fixPropertyDeclaration(): String =
    replace(DESCRIPTOR_REGEX, "$1 configurable: true,")
