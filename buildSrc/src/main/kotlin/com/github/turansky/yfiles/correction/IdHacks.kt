package com.github.turansky.yfiles.correction

import java.io.File

internal fun generateIdUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/lang/Id.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.lang
                |
                |external interface Id
                |
                |fun Id(source:Any):Id = 
                |    source.unsafeCast<Id>()
            """.trimMargin()
        )
}

@Suppress("UNUSED_PARAMETER")
internal fun applyIdHacks(source: Source) {
    // implement
}
