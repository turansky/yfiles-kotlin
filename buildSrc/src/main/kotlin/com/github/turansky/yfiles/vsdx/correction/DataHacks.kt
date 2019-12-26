package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.GeneratorContext

internal const val MASTER_STATE = "MasterState"
internal const val IMAGE_DATA = "ImageData"

internal fun createVsdxDataClasses(context: GeneratorContext) {
    // language=kotlin
    context["yfiles.vsdx.$MASTER_STATE"] = """
            |package yfiles.vsdx
            |
            |external interface $MASTER_STATE {
            |    val master: Master
            |    val fillStyle: StyleSheet
            |    val lineStyle: StyleSheet
            |    val textStyle: StyleSheet
            |}
        """.trimMargin()

    // language=kotlin
    context["yfiles.vsdx.$IMAGE_DATA"] = """
            |package yfiles.vsdx
            |
            |external interface $IMAGE_DATA {
            |    val data: String
            |    val format: String
            |}
        """.trimMargin()
}
