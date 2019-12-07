package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import java.io.File

private const val CONVERTERS = "yfiles.styles.Converters"

internal fun generateConvertersUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/styles/Converters.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.styles
                |
                |@JsName("Object")
                |external class Converters
                |
                |operator fun <V, R : Any> Converters.set(
                |    name: String,
                |    converter: (value: V) -> R
                |) {
                |    asDynamic()[name] = converter
                |}
                |
                |operator fun <V, P, R : Any> Converters.set(
                |    name: String,
                |    converter: (value: V, parameter: P) -> R
                |) {
                |    asDynamic()[name] = converter
                |}
            """.trimMargin()
        )
}

internal fun applyConvertersHacks(source: Source) {
    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    source.types()
        .filter { it[ID].startsWith("yfiles.styles.") }
        .optFlatMap(CONSTANTS)
        .filter { it[NAME] == "CONVERTERS" }
        .filter { it[TYPE] in likeObjectTypes }
        .forEach { it[TYPE] = CONVERTERS }
}