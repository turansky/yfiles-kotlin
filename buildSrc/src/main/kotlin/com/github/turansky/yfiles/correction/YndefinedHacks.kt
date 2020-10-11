package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

internal fun generateYndefined(context: GeneratorContext) {
    // language=kotlin
    context["yfiles.lang.yndefined"] =
        """
            inline fun <T> yndefined(): T = 
                null.unsafeCast<T>()
        """.trimIndent()
}
