package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.GeneratorContext

internal const val MASTER_STATE = "MasterState"
internal const val IMAGE_DATA_RESPONSE = "ImageDataResponse"

internal fun createVsdxDataClasses(context: GeneratorContext) {
    // language=kotlin
    context["yfiles.vsdx.$MASTER_STATE"] = """
            external interface $MASTER_STATE {
                val master: Master
                val fillStyle: StyleSheet
                val lineStyle: StyleSheet
                val textStyle: StyleSheet
            }
        """.trimIndent()

    // language=kotlin
    context["yfiles.vsdx.$IMAGE_DATA_RESPONSE"] = """
            external interface $IMAGE_DATA_RESPONSE {
                val data: String
                val format: String
            }
        """.trimIndent()
}
