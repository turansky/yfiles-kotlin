package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.Class

private val MULTIPLIERS = listOf(
    24, 60, 60, 1000
)

internal fun timeSpanExtensions(timeSpanClass: Class): String {
    val parameters = timeSpanClass.secondaryConstructors
        .maxBy { it.parameters.size }!!
        .parameters

    return parameters
        .zipWithNext { current, next ->
            val multiplier = MULTIPLIERS[parameters.indexOf(current)]
            """
                inline val Int.${current.name}: TimeSpan
                    get() = (this * $multiplier).${next.name}
            """.trimIndent()
        }
        .joinToString(separator = "\n\n", postfix = "\n\n") +
            """
                inline val Int.${parameters.last().name}: TimeSpan
                    get() = TimeSpan(this)
            """.trimIndent()
}
