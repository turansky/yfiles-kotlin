package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.removeItem
import org.json.JSONObject

private val EXCLUDED_TYPES = setOf(
    "yfiles.lang.Abstract",
    "yfiles.lang.Struct",

    "yfiles.lang.AttributeDefinition",

    "yfiles.lang.EnumDefinition",

    "yfiles.lang.Interface",
    "yfiles.lang.InterfaceDefinition",

    "yfiles.lang.ClassDefinition",

    "yfiles.lang.delegate",
    "yfiles.lang.Exception",
    "yfiles.lang.Trait"
)

internal fun excludeUnusedTypes(api: JSONObject) {
    api
        .flatMap(NAMESPACES)
        .optFlatMap(NAMESPACES)
        .forEach {
            val types = it[TYPES]

            val excludedTypes = types
                .asSequence()
                .map { it as JSONObject }
                .filter { it[ID] in EXCLUDED_TYPES }
                .toList()

            excludedTypes.forEach {
                types.removeItem(it)
            }
        }
}
