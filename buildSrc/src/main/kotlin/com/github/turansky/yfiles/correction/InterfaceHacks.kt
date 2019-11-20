package com.github.turansky.yfiles.correction

import java.io.File

internal fun generateInterfaceMarker(sourceDir: File) {
    sourceDir.resolve("yfiles/lang/Interface.kt")
        .apply { parentFile.mkdirs() }
        .writeText(
            // language=kotlin
            """
                |package yfiles.lang
                |
                |@Target(AnnotationTarget.CLASS)
                |@Retention(AnnotationRetention.BINARY)
                |internal annotation class Interface
            """.trimMargin()
        )
}
