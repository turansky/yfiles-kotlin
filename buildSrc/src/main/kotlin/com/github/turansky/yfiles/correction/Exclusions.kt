package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.removeItem
import org.json.JSONObject

private val EXCLUDED_TYPES = setOf(
    "yfiles.lang.Struct",

    "yfiles.lang.AttributeDefinition",

    "yfiles.lang.Enum",
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
        .jsequence(J_NAMESPACES)
        .optionalArray(J_NAMESPACES)
        .forEach {
            val types = it.getJSONArray(J_TYPES)

            val excludedTypes = types
                .asSequence()
                .map { it as JSONObject }
                .filter { it.getString(J_ID) in EXCLUDED_TYPES }
                .toList()

            excludedTypes.forEach {
                types.removeItem(it)
            }
        }
}