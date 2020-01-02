package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.IENUMERABLE
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_BOOLEAN

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
    val typeMap = mapOf(
        "BENDS" to "$IENUMERABLE<*>",
        "IS_GROUP_NODE" to JS_BOOLEAN,
        "LABELS" to "$IENUMERABLE<*>",
        "LAYOUT" to "yfiles.geometry.Rect",
        "PORT_LOCATION_MODEL_PARAMETER" to "yfiles.graph.IPortLocationModelParameter",
        "STYLE" to JS_ANY,
        "TAG" to TAG
    )

    source.types("CreationProperties")
        .flatMap(CONSTANTS)
        .forEach { it[TYPE] = propertyKey(typeMap.getValue(it[NAME])) }
}
