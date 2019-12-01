package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.PARTITION_CELL_KEY
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

internal fun applyPartitionCellHacks(source: Source) {
    val methodNames = setOf(
        "getData",
        "putData",
        "removeData"
    )

    source.type("PartitionCell")
        .flatMap(METHODS)
        .filter { it[NAME] in methodNames }
        .onEach { it.setSingleTypeParameter(bound = JS_OBJECT) }
        .onEach { it[RETURNS][TYPE] = "T" }
        .onEach { it.changeNullability(true) }
        .flatMap(PARAMETERS)
        .forEach {
            it[TYPE] = when (val name = it[NAME]) {
                "key" -> "$PARTITION_CELL_KEY<T>"
                "data" -> "T"
                else -> throw IllegalArgumentException("Unable to calculate type by name '$name'")
            }
        }
}
