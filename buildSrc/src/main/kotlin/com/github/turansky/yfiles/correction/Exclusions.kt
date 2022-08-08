package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.removeAllObjects
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
    "yfiles.lang.Trait",

    "yfiles.styles.BevelNodeStyle",
    "yfiles.styles.BevelNodeStyleRenderer",
    "yfiles.styles.PanelNodeStyle",
    "yfiles.styles.PanelNodeStyleRenderer",
    "yfiles.styles.ShinyPlateNodeStyle",
    "yfiles.styles.ShinyPlateNodeStyleRenderer",
)

internal fun excludeUnusedTypes(api: JSONObject) {
    api[TYPES].removeAllObjects {
        it[ID] in EXCLUDED_TYPES
    }
}
