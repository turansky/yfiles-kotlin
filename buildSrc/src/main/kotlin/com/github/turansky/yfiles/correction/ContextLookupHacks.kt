package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IMODEL_ITEM
import com.github.turansky.yfiles.json.get

private const val ICONTEXT_LOOKUP = "yfiles.graph.IContextLookup"

internal fun applyContextLookupHacks(source: Source) {
    source.type("IContextLookup") {
        setSingleTypeParameter("in TItem", IMODEL_ITEM)

        method("contextLookup")
            .parameter("item")[TYPE] = "TItem"

        get(CONSTANTS)["EMPTY_CONTEXT_LOOKUP"]
            .addGeneric("*")
    }

    source.types("DefaultGraph", "Table")
        .flatMap(CONSTANTS)
        .filter { it[TYPE] == ICONTEXT_LOOKUP }
        .filter { it[NAME].let { it.startsWith("DEFAULT_") && it.endsWith("_LOOKUP") } }
        .forEach {
            val typeParameter = it[NAME]
                .removePrefix("DEFAULT_")
                .removeSuffix("_LOOKUP")
                .toLowerCase()
                .capitalize()
                .let { "yfiles.graph.I$it" }

            it.addGeneric(typeParameter)
        }

    source.types(
        "TemplateLabelStyleBase",
        "TemplateNodeStyleBase",
        "TemplatePortStyleBase",
        "TemplateStripeStyleBase"
    ).forEach {
        val typeParameter = it[NAME]
            .removePrefix("Template")
            .removeSuffix("StyleBase")
            .let { "yfiles.graph.I$it" }

        it.property("contextLookup")
            .addGeneric(typeParameter)
    }

    source.types(
        "TemplateNodeStyleRenderer",
        "TemplateStripeStyleRenderer"
    ).forEach {
        val typeParameter = it[NAME]
            .removePrefix("Template")
            .removeSuffix("StyleRenderer")
            .let { "yfiles.graph.I$it" }

        it.method("getContextLookup")
            .get(RETURNS)
            .addGeneric(typeParameter)
    }

    source.type("IContextLookupChainLink") {
        get(IMPLEMENTS).also {
            it.put(0, it.getString(0) + "<$IMODEL_ITEM>")
        }

        method("setNext")
            .parameter("next")
            .addGeneric(IMODEL_ITEM)
    }

    source.type("LookupChain") {
        get(IMPLEMENTS).also {
            it.put(0, it.getString(0) + "<$IMODEL_ITEM>")
        }

        method("contextLookup")
            .parameter("item")[TYPE] = IMODEL_ITEM
    }
}
