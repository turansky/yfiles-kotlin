package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.YENUM
import java.io.File

internal fun generateFlagsUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/lang/Flags.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.lang
                |
                |external interface Flags<T>
                |   where T : Flags<T>,
                |         T : $YENUM<T>
                |         
                |inline infix fun <T> Flags<T>.or(other: Flags<T>): Flags<T>
                |   where T : Flags<T>,
                |         T : yfiles.lang.YEnum<T> {
                |   return unsafeCast<Int>()
                |       .or(other.unsafeCast<Int>())
                |       .unsafeCast<T>()
                |}
            """.trimMargin()
        )
}
