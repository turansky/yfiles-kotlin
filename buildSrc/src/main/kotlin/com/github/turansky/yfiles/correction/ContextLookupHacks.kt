package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.YOBJECT
import com.github.turansky.yfiles.json.get

private const val ICONTEXT_LOOKUP = "yfiles.graph.IContextLookup"
private const val T_ITEM = "TItem"
private const val T_ITEM_BOUND = YOBJECT

private const val LOOKUP_CALLBACK = "yfiles.graph.LookupCallback"

internal fun applyContextLookupHacks(source: Source) {
    source.type("IContextLookup") {
        setSingleTypeParameter("in $T_ITEM", T_ITEM_BOUND)

        method("contextLookup")
            .parameter("item")[TYPE] = T_ITEM

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
            it.put(0, it.getString(0) + "<$T_ITEM_BOUND>")
        }

        method("setNext")
            .parameter("next")
            .addGeneric(T_ITEM_BOUND)

        method("createContextLookupChainLink")
            .firstParameter
            .also { it[SIGNATURE] = it[SIGNATURE] + "<*>" }
    }

    source.type("LookupChain") {
        setSingleTypeParameter(T_ITEM, T_ITEM_BOUND)

        get(IMPLEMENTS).also {
            it.put(0, it.getString(0) + "<$T_ITEM>")
        }

        method("contextLookup")
            .parameter("item")[TYPE] = T_ITEM
    }

    source.type("CanvasComponent")
        .property("inputModeContextLookupChain")
        .addGeneric("CanvasComponent")

    source.functionSignature(LOOKUP_CALLBACK).apply {
        setSingleTypeParameter()

        firstParameter[TYPE] = "T"
    }

    source.type("IInputModeContext")
        .flatMap(METHODS)
        .filter { it[NAME] == "createInputModeContext" }
        .flatMap(PARAMETERS)
        .filter { it.opt(SIGNATURE) == LOOKUP_CALLBACK }
        .forEach { it[SIGNATURE] = "$LOOKUP_CALLBACK<*>" }
}
