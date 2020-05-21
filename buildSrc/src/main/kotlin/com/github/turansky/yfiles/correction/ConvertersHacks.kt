package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT

private const val CONVERTERS = "yfiles.styles.Converters"

internal fun generateConvertersUtils(context: GeneratorContext) {
    // language=kotlin
    context[CONVERTERS] =
        """
            |@JsName("Object")
            |sealed external class Converters
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
}

internal fun applyConvertersHacks(source: Source) {
    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    source.type(TEMPLATES_NAME)
        .flatMap(CorrectionMode.getValue(CONSTANTS, PROPERTIES))
        .first { it[NAME] == "CONVERTERS" }
        .also { check(it[TYPE] in likeObjectTypes) }
        .set(TYPE, CONVERTERS)
}
