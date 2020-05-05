package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.Class
import com.github.turansky.yfiles.ContentMode.EXTENSIONS
import com.github.turansky.yfiles.GeneratorContext

private val MULTIPLIERS = listOf(
    24, 60, 60, 1000
)

internal fun generateTimeSpanExtensions(context: GeneratorContext, timeSpanClass: Class) {
    val parameters = timeSpanClass.secondaryConstructors
        .maxBy { it.parameters.size }!!
        .parameters

    val content = parameters
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


    // language=kotlin
    context[timeSpanClass.classId, EXTENSIONS] = content
}
