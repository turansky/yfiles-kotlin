package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.JS_STRING
import java.io.File

private const val SERIALIZATION_PROPERTY_KEY = "yfiles.graphml.SerializationPropertyKey"

private fun propertyKey(typeParameter: String) =
    "$SERIALIZATION_PROPERTY_KEY<$typeParameter>"

internal fun generateSerializationUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/graphml/SerializationPropertyKey.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.graphml
                |
                |@JsName("String")
                |external class SerializationPropertyKey<T : Any> internal constructor()
            """.trimMargin()
        )
}

internal fun applySerializationHacks(source: Source) {
    source.types()
        .filter { it[ID].startsWith("yfiles.graphml.") }
        .optFlatMap(METHODS)
        .filter { it.has(PARAMETERS) }
        .filter { it.firstParameter.let { it[NAME] == "key" && it[TYPE] == JS_STRING } }
        .forEach {
            val size = it[PARAMETERS].length()
            if (size == 1 && !it.has(RETURNS)) {
                it.firstParameter[TYPE] = propertyKey("*")
            } else {
                it.setSingleTypeParameter(bound = JS_OBJECT)
                it.firstParameter[TYPE] = propertyKey("T")
                if (size == 2) {
                    it.secondParameter[TYPE] = "T"
                }
                if (it.has(RETURNS)) {
                    it[RETURNS][TYPE] = "T"
                }
            }
        }
}
