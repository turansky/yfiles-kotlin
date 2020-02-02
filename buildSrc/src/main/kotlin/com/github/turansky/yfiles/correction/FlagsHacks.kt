package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.INLINE
import com.github.turansky.yfiles.GeneratorContext

internal fun generateFlagsUtils(context: GeneratorContext) {
    // language=kotlin
    context["yfiles.lang.Flags", INLINE] =
        """
            |external interface Flags<T>
            |        where T : Flags<T>,
            |              T : YEnum<T>
            |
            |inline val Flags<*>.v:Int
            |   get() = unsafeCast<Int>()
            |
            |inline infix fun <T> T.or(other: T): T
            |        where T : Flags<T>,
            |              T : YEnum<T> {
            |    return (v or other.v).unsafeCast<T>()
            |}
            |
            |inline operator fun <T> T.contains(other: T): Boolean
            |        where T : Flags<T>,
            |              T : YEnum<T> {
            |    return (v and other.v) == other.v
            |}
        """.trimMargin()
}
