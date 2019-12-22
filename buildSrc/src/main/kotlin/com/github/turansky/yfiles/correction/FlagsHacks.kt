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
            """.trimMargin()
        )
}
