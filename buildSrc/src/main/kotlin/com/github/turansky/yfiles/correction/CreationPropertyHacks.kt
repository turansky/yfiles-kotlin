package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import com.github.turansky.yfiles.json.strictRemove

private const val CREATION_PROPERTY_KEY = "yfiles.graphml.CreationPropertyKey"

private fun propertyKey(typeParameter: String) =
    "$CREATION_PROPERTY_KEY<$typeParameter>"

internal fun generateCreationPropertyUtils(context: GeneratorContext) {
    // language=kotlin
    context[CREATION_PROPERTY_KEY] = """
            |@JsName("String")
            |sealed external class CreationPropertyKey<T : Any> 
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

    source.type("CreationProperties").apply {
        strictRemove(IMPLEMENTS)

        get(PROPERTIES)["entries"].also {
            it.replaceInType("<$JS_ANY,", "<${propertyKey("*")},")
        }

        flatMap(METHODS)
            .filter { it.has(PARAMETERS) }
            .forEach {
                val typeParameter = when (it[NAME]) {
                    "removeValue" -> "*"
                    else -> {
                        it.setSingleTypeParameter(bound = JS_OBJECT)
                        "T"
                    }
                }

                if (it[NAME] == "get") {
                    it[RETURNS][TYPE] = "T"
                }

                it.flatMap(PARAMETERS)
                    .forEach {
                        it[TYPE] = when (it[NAME]) {
                            "key" -> propertyKey(typeParameter)
                            "value" -> typeParameter
                            else -> TODO()
                        }
                    }
            }

        flatMap(CONSTANTS)
            .forEach { it[TYPE] = propertyKey(typeMap.getValue(it[NAME])) }
    }
}
