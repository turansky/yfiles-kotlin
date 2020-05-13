package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.INLINE
import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.YENUM
import com.github.turansky.yfiles.YFLAGS

internal fun generateFlagsUtils(context: GeneratorContext) {
    // language=kotlin
    context[YFLAGS, INLINE] =
        """
            |external interface YFlags<T: YFlags<T>> : $YENUM<T>
            |
            |inline val YFlags<*>.v:Int
            |   get() = unsafeCast<Int>()
            |
            |inline infix fun <T : YFlags<T>> T.or(other: T): T =
            |    (v or other.v).unsafeCast<T>()
            |
            |inline operator fun <T : YFlags<T>> T.contains(other: T): Boolean =
            |    (v and other.v) == other.v
        """.trimMargin()
}
