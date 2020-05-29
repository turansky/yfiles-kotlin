package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_VOID
import com.github.turansky.yfiles.YOBJECT
import com.github.turansky.yfiles.json.get

private const val ICONTEXT_LOOKUP = "yfiles.graph.IContextLookup"
private const val ICONTEXT_LOOKUP_CHAIN_LINK = "yfiles.graph.IContextLookupChainLink"

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

    source.type("LookupChain") {
        setSingleTypeParameter(T_ITEM, T_ITEM_BOUND)

        get(IMPLEMENTS).also {
            it.put(0, it.getString(0) + "<$T_ITEM>")
        }

        method("contextLookup")
            .parameter("item")[TYPE] = T_ITEM

        flatMap(METHODS)
            .flatMap { it.optFlatMap(PARAMETERS) + sequenceOf(it.opt(RETURNS)).filterNotNull() }
            .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
            .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<$T_ITEM>" }
    }

    source.type("CanvasComponent")
        .property("inputModeContextLookupChain")
        .addGeneric("CanvasComponent")

    source.functionSignature(LOOKUP_CALLBACK).apply {
        setSingleTypeParameter()

        firstParameter[TYPE] = "T"
        get(RETURNS)[TYPE] = YOBJECT
    }

    source.type("IInputModeContext")
        .flatMap(METHODS)
        .filter { it[NAME] == "createInputModeContext" }
        .flatMap(PARAMETERS)
        .filter { it.opt(SIGNATURE) == LOOKUP_CALLBACK }
        .forEach { it[SIGNATURE] = "$LOOKUP_CALLBACK<$JS_VOID>" }

    source.type("IContextLookupChainLink") {
        setSingleTypeParameter(T_ITEM, T_ITEM_BOUND)

        get(IMPLEMENTS).also {
            it.put(0, it.getString(0) + "<$T_ITEM>")
        }

        method("setNext")
            .parameter("next")
            .addGeneric(T_ITEM)

        method("createContextLookupChainLink").apply {
            setSingleTypeParameter(bound = YOBJECT)

            firstParameter.also { it[SIGNATURE] = it[SIGNATURE] + "<T>" }
            get(RETURNS).also { it[TYPE] = it[TYPE] + "<T>" }
        }

        flatMap(METHODS)
            .mapNotNull { it.opt(RETURNS) }
            .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
            .forEach { it[TYPE] = it[TYPE] + "<*>" }
    }

    source.types(
        "NodeDecorator",
        "EdgeDecorator",
        "PortDecorator",
        "LabelDecorator",
        "BendDecorator",
        "StripeDecorator",
        "StripeLabelDecorator"
    ).forEach {
        val typeParameter = it[NAME]
            .replace("StripeLabel", "Label")
            .removeSuffix("Decorator")
            .let { "yfiles.graph.I$it" }

        it.method("remove")
            .firstParameter
            .also { it[TYPE] = it[TYPE] + "<${typeParameter}>" }
    }

    source.type("LookupDecorator")
        .flatMap(METHODS)
        .flatMap { it.flatMap(PARAMETERS) + it[RETURNS] }
        .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
        .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<TDecoratedType>" }

    source.type("ILookupDecorator").apply {
        sequenceOf("addLookup", "removeLookup")
            .map { method(it) }
            .onEach { it.setSingleTypeParameter(bound = YOBJECT) }
            .flatMap(PARAMETERS)
            .forEach { it[TYPE] = it[TYPE] + "<T>" }

        flatMap(METHODS)
            .mapNotNull { it.opt(RETURNS) }
            .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
            .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<*>" }
    }

    source.type("DefaultGraph")
        .flatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
        .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<DefaultGraph>" }
}
