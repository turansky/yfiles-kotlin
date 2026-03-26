package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.get

private const val ICONTEXT_LOOKUP = "yfiles.collections.IContextLookup"
private const val ICONTEXT_LOOKUP_CHAIN_LINK = "yfiles.collections.IContextLookupChainLink"

private const val T_ITEM = "TItem"
private const val T_DECORATED_TYPE = "TDecoratedType"


internal fun applyContextLookupHacks(source: Source) {
    source.types()
        .optFlatMap(CONSTANTS)
        .filter { it[TYPE] == ICONTEXT_LOOKUP }
        .forEach { it[TYPE] = "$ICONTEXT_LOOKUP<*>" }

    val lookupMethods = setOf("addGenericLookup", "addLookup", "removeLookup", "addGenericInputModeContextLookup")
    source.types()
        .optFlatMap(METHODS)
        .filter { it[NAME] in lookupMethods }
        .forEach { method ->

            if (method[NAME] == "addGenericLookup" || method[NAME] == "addGenericInputModeContextLookup") {
                method.firstParameter
                    .also {
                        it[TYPE] = it[TYPE].replace(ICONTEXT_LOOKUP, "$ICONTEXT_LOOKUP<*>")
                        it[SIGNATURE] = it[SIGNATURE].replace(ICONTEXT_LOOKUP, "$ICONTEXT_LOOKUP<*>")
                    }

                method[RETURNS]
                    .also {
                        it[TYPE] = it[TYPE].replace(ICONTEXT_LOOKUP_CHAIN_LINK, "$ICONTEXT_LOOKUP_CHAIN_LINK<*>")
                    }
            }

            method.flatMap(PARAMETERS)
                .filter { it[TYPE] == ICONTEXT_LOOKUP }
                .forEach { it[TYPE] = "$ICONTEXT_LOOKUP<*>" }

            method.flatMap(PARAMETERS)
                .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
                .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<*>" }
        }

    source.type("IContextLookup") {
        setSingleTypeParameter("in $T_ITEM")

        method("contextLookup")
            .parameter("item")[TYPE] = T_ITEM

        method("create")[RETURNS].addGeneric(T_ITEM)
    }

    source.types("Table")
        .flatMap(CONSTANTS)
        .filter { it[TYPE] == ICONTEXT_LOOKUP }
        .filter { it[NAME].let { it.startsWith("DEFAULT_") && it.endsWith("_LOOKUP") } }
        .forEach {
            val typeParameter = it[NAME]
                .removePrefix("DEFAULT_")
                .removeSuffix("_LOOKUP")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
                .let { "yfiles.graph.I$it" }

            it.addGeneric(typeParameter)
        }

    source.type("IContextLookupChainLink") {
        setSingleTypeParameter(T_ITEM)

        get(IMPLEMENTS).also {
            it.put(0, it.getString(0) + "<$T_ITEM>")
        }

        method("setNext")
            .parameter("next")
            .addGeneric(T_ITEM)

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
        it.method("add")
            .firstParameter
            .also { it[TYPE] = it[TYPE] + "<${typeParameter}>" }
    }

    source.type("LookupDecorator")
        .flatMap(METHODS)
        .flatMap { it.flatMap(PARAMETERS) + it[RETURNS] }
        .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
        .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<$T_DECORATED_TYPE>" }

    source.type("ILookupDecorator")
        .flatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
        .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<*>" }

    source.type("ContextLookup").apply {
        set(IMPLEMENTS, arrayOf("yfiles.collections.IContextLookup<$T_DECORATED_TYPE>"))

        method("contextLookup")
            .parameter("item")[TYPE] = T_DECORATED_TYPE

        sequenceOf("addLookup", "removeLookup")
            .map { method(it) }
            .flatMap { it.flatMap(PARAMETERS) }
            .filter { it[TYPE] == "$ICONTEXT_LOOKUP<*>" }
            .forEach { it[TYPE] = "$ICONTEXT_LOOKUP<$T_DECORATED_TYPE>" }
        sequenceOf("addLookup", "removeLookup")
            .map { method(it) }
            .flatMap { it.flatMap(PARAMETERS) }
            .filter { it[TYPE] == "$ICONTEXT_LOOKUP_CHAIN_LINK<*>" }
            .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<$T_DECORATED_TYPE>" }

        flatMap(METHODS)
            .mapNotNull { it.opt(RETURNS) }
            .filter { it[TYPE] == ICONTEXT_LOOKUP_CHAIN_LINK }
            .forEach { it[TYPE] = "$ICONTEXT_LOOKUP_CHAIN_LINK<$T_DECORATED_TYPE>" }
    }
}
