package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.jObject
import org.json.JSONObject

private const val TEMPLATE_LOADERS = "yfiles.styles.TemplateLoaders"
private val TEMPLATE_LOADERS_NAME = TEMPLATE_LOADERS.substringAfterLast(".")
internal const val TEMPLATE_LOADERS_ALIAS = "TemplateNodeStyleBase"

private val COPIED_NAMES = setOf(
    "loadAllTemplates"
)

internal fun applyTemplateLoadersHacks(source: Source) {
    source.add(createTemplates(source.type(TEMPLATE_LOADERS_ALIAS)))

    source.types()
        .filter { it[ID].startsWith("yfiles.styles.") }
        .filter { it[NAME].startsWith("Template") }
        .filter { it[NAME].endsWith("StyleBase") }
        .forEach { it.removeCommonItems() }
}

private fun JSONObject.removeCommonItems() {
    get(METHODS).removeAll {
        (it as JSONObject)[NAME] in COPIED_NAMES
    }
}

private fun createTemplates(sourceType: JSONObject): JSONObject {
    return jObject(
        ID to TEMPLATE_LOADERS,
        NAME to TEMPLATE_LOADERS_NAME,
        ES6_NAME to TEMPLATE_LOADERS_ALIAS,
        GROUP to "class"
    ).also { type ->
        type[METHODS] = sourceType.flatMap(METHODS)
            .filter { it[NAME] in COPIED_NAMES }
            .toList()
    }
}
