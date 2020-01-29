package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

internal fun generateResourceTypes(
    devguide: JSONObject,
    context: GeneratorContext
) {
    val content = devguide
        .flatMap(CHILDREN)
        .optFlatMap(CHILDREN)
        .optFlatMap(CHILDREN)
        .optFlatMap(CHILDREN)
        .first { it.opt(ID) == "tab_resource_defaults" }
        .get(CONTENT)

    context["yfiles.Resources"] = """
        /*
        $content
        */
    """.trimIndent()
}
