package com.github.turansky.yfiles.correction

import java.io.File

internal fun generatePartitionCellUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/router/PartitionCellKey.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.router
                |
                |external interface PartitionCellKey<T:Any>
                |
                |fun <T:Any> PartitionCellKey(source:Any):PartitionCellKey<T> = 
                |    source.unsafeCast<PartitionCellKey<T>>()
            """.trimMargin()
        )
}

@Suppress("UNUSED_PARAMETER")
internal fun applyPartitionCellHacks(source: Source) {
    // implement
}
