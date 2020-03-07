package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*

private const val SERIALIZATION_PROPERTY_KEY = "yfiles.graphml.SerializationPropertyKey"

private fun propertyKey(typeParameter: String) =
    "$SERIALIZATION_PROPERTY_KEY<$typeParameter>"

internal fun generateSerializationUtils(context: GeneratorContext) {
    // language=kotlin
    context[SERIALIZATION_PROPERTY_KEY] = """
            |@JsName("String")
            |sealed external class SerializationPropertyKey<T : Any> 
        """.trimMargin()
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

    val typeMap = mapOf(
        "BASE_URI" to JS_STRING,
        "CACHE_EXTERNAL_REFERENCES" to JS_BOOLEAN,
        "CURRENT_KEY_SCOPE" to "yfiles.graphml.KeyScope",
        "DISABLE_GEOMETRY" to GRAPH_ITEM_TYPES,
        "DISABLE_GRAPH_SETTINGS" to JS_BOOLEAN,
        "DISABLE_ITEMS" to GRAPH_ITEM_TYPES,
        "DISABLE_STRIPE_LABELS" to STRIPE_TYPES,
        "DISABLE_STRIPE_STYLES" to STRIPE_TYPES,
        "DISABLE_STRIPE_USER_TAGS" to STRIPE_TYPES,
        "DISABLE_STYLES" to GRAPH_ITEM_TYPES,
        "DISABLE_USER_TAGS" to GRAPH_ITEM_TYPES,
        "IGNORE_PROPERTY_CASE" to JS_BOOLEAN,
        "IGNORE_XAML_DESERIALIZATION_ERRORS" to JS_BOOLEAN,
        "INDENT_OUTPUT" to JS_BOOLEAN,
        "PARSE_LABEL_SIZE" to JS_BOOLEAN,
        "REPRESENTED_EDGE" to IEDGE,
        "REWRITE_RELATIVE_RESOURCE_URIS" to JS_BOOLEAN,
        "UNDEFINED_HANDLING" to "yfiles.graphml.UndefinedHandling",
        "WRITE_EDGE_STYLE_DEFAULT" to JS_BOOLEAN,
        "WRITE_LABEL_SIZE_PREDICATE" to "yfiles.lang.Predicate<$ILABEL>",
        "WRITE_NODE_STYLE_DEFAULT" to JS_BOOLEAN,
        "WRITE_PORT_STYLE_DEFAULT" to JS_BOOLEAN,
        "WRITE_STRIPE_DEFAULTS" to JS_BOOLEAN
    )

    source.type("SerializationProperties")
        .flatMap(CONSTANTS)
        .forEach { it[TYPE] = propertyKey(typeMap.getValue(it[NAME])) }
}
