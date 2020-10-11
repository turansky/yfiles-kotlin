package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.YFLAGS

internal fun generateFlagsUtils(context: GeneratorContext) {
    // language=kotlin
    context[YFLAGS] =
        """
            external interface YFlags<T: YFlags<T>>
            
            inline val YFlags<*>.value:Int
               get() = unsafeCast<Int>()
            
            inline infix fun <T : YFlags<T>> T.or(other: T): T =
                (value or other.value).unsafeCast<T>()
            
            inline operator fun <T : YFlags<T>> T.contains(other: T): Boolean =
                (value and other.value) == other.value
        """.trimIndent()
}
