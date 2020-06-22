package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.INTERNAL
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.removeAllObjects
import org.json.JSONObject

private const val TEMPLATES = "yfiles.styles.Templates"
internal val TEMPLATES_NAME = TEMPLATES.substringAfterLast(".")
private const val TEMPLATES_ALIAS = "StringTemplateNodeStyle"

private val COPIED_KEYS = setOf(
    CONSTANTS,
    PROPERTIES,
    METHODS
)

private val COPIED_NAMES = setOf(
    "CONVERTERS",
    "trusted",
    "makeObservable"
)

internal fun applyTemplatesHacks(source: Source) {
    source.add(createTemplates(source.type(TEMPLATES_ALIAS)))

    source.types()
        .filter { it[ID].startsWith("yfiles.styles.") }
        .filter { it[NAME].contains("Template") }
        .filter { it[NAME].endsWith("Style") }
        .forEach { it.removeCommonItems() }
}

private fun JSONObject.removeCommonItems() {
    COPIED_KEYS
        .filter { has(it) }
        .forEach { key ->
            get(key).removeAllObjects {
                it[NAME] in COPIED_NAMES
            }
        }
}

private fun createTemplates(sourceType: JSONObject): JSONObject {
    val type = jObject(
        ID to TEMPLATES,
        NAME to TEMPLATES_NAME,
        ES6_NAME to TEMPLATES_ALIAS,
        GROUP to "class"
    )

    COPIED_KEYS.forEach { key ->
        type[key] = sourceType.flatMap(key)
            .filter { it[NAME] in COPIED_NAMES }
            .toList()
    }

    type.method("makeObservable")
        .get(MODIFIERS).put(INTERNAL)

    return type
}
