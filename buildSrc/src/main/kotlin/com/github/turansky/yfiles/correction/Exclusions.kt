package com.github.turansky.yfiles.correction

import org.json.JSONObject

private val EXCLUDED_TYPES = setOf(
    "yfiles.lang.Abstract",
    "yfiles.lang.Struct",

    "yfiles.lang.AttributeDefinition",

    "yfiles.lang.EnumDefinition",

    "yfiles.lang.Interface",
    "yfiles.lang.InterfaceDefinition",

    "yfiles.lang.ClassDefinition",
    "yfiles.lang.PropertyInfo",

    "yfiles.lang.delegate",
    "yfiles.lang.Exception",
    "yfiles.lang.Trait"
)

internal fun excludeUnusedTypes(api: JSONObject) {
    api[TYPES].removeAll {
        it as JSONObject
        it[ID] in EXCLUDED_TYPES
    }
}
