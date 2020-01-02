package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

private const val CREATION_PROPERTY_KEY = "yfiles.graphml.CreationPropertyKey"

private fun propertyKey(typeParameter: String) =
    "$CREATION_PROPERTY_KEY<$typeParameter>"

internal fun generateCreationPropertyUtils(context: GeneratorContext) {
    // language=kotlin
    context[CREATION_PROPERTY_KEY] = """
            |package yfiles.graphml
            |
            |@JsName("String")
            |external class CreationPropertyKey<T : Any> 
            |internal constructor()
            |
            |fun <T : Any> CreationPropertyKey(source: String): CreationPropertyKey<T> =
            |    source.unsafeCast<CreationPropertyKey<T>>()
        """.trimMargin()
}

internal fun applyCreationPropertyHacks(source: Source) {
    source.types()
        .filter { it[ID].startsWith("yfiles.graphml.") }
}
