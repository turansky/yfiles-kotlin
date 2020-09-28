package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*

private const val CONVERTERS = "yfiles.styles.Converters"

internal fun generateConvertersUtils(context: GeneratorContext) {
    // language=kotlin
    context[CONVERTERS] =
        """
            @JsName("Object")
            external class Converters
            private constructor()
            
            inline operator fun Converters.invoke(
                block: Converters.() -> Unit
            ): Converters =
               apply(block)
            
            fun <V, R : Any> Converters.put(
                name: String,
                converter: (value: V) -> R
            ): Converters {
                $AS_DYNAMIC[name] = converter
                return this
            }
            
            fun <V, P: $STRING?, R : Any> Converters.put(
                name: String,
                converter: (value: V, parameter: P) -> R
            ): Converters {
                $AS_DYNAMIC[name] = converter
                return this
            }
        """.trimIndent()
}

internal fun applyConvertersHacks(source: Source) {
    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    source.type(TEMPLATES_NAME)
        .flatMap(CONSTANTS)
        .first { it[NAME] == "CONVERTERS" }
        .also { check(it[TYPE] in likeObjectTypes) }
        .set(TYPE, CONVERTERS)
}
