// TODO: Remove after fix https://youtrack.jetbrains.com/issue/KT-34770
package com.github.turansky.yfiles.gradle.plugin

private val DESCRIPTOR_REGEX = Regex("(Object\\.defineProperty\\(.+\\.prototype, '[a-zA-Z]+', \\{)")
private val CONFIGURABLE_REGEX = Regex("\\s+configurable: true,")

internal fun String.fixPropertyDeclaration(): String =
    when {
        CONFIGURABLE_REGEX.matches(this) -> ""
        else -> replace(DESCRIPTOR_REGEX, "$1 configurable: true,")
    }
