package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.INLINE
import com.github.turansky.yfiles.GeneratorContext

internal const val YFLAGS = "yfiles.lang.YFlags"

internal fun generateFlagsUtils(context: GeneratorContext) {
    // language=kotlin
    context[YFLAGS, INLINE] =
        """
            |external interface YFlags<T>
            |        where T : YFlags<T>,
            |              T : YEnum<T>
            |
            |inline val YFlags<*>.v:Int
            |   get() = unsafeCast<Int>()
            |
            |inline infix fun <T> T.or(other: T): T
            |        where T : YFlags<T>,
            |              T : YEnum<T> {
            |    return (v or other.v).unsafeCast<T>()
            |}
            |
            |inline operator fun <T> T.contains(other: T): Boolean
            |        where T : YFlags<T>,
            |              T : YEnum<T> {
            |    return (v and other.v) == other.v
            |}
        """.trimMargin()
}
