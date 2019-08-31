package com.github.turansky.yfiles.vsdx.correction

import java.io.File

internal const val MASTER_STATE = "MasterState"

internal fun createVsdxDataClasses(sourceDir: File) {
    sourceDir.resolve("yfiles/vsdx/$MASTER_STATE.kt")
        .writeText(
            """
                |package yfiles.vsdx
                |
                |external interface $MASTER_STATE {
                |    val master: Master
                |    val fillStyle: StyleSheet
                |    val lineStyle: StyleSheet
                |    val textStyle: StyleSheet
                |}
            """.trimMargin()
        )
}