package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import org.json.JSONObject

internal fun applyClassHacks(source: Source) {
    addClassGeneric(source)
    addConstructorClassGeneric(source)
    addMethodClassGeneric(source)
    addMapperMetadataGeneric(source)

    removeUnusedTypeParameters(source)
    addClassBounds(source)

    addTypeParameterBounds(source)
}

private fun addClassGeneric(source: Source) {
    source.type("Class").apply {
        setSingleTypeParameter()

        getJSONArray(J_METHODS)
            .firstWithName("newInstance")
            .getJSONObject(J_RETURNS)
            .put(J_TYPE, "T")
    }

    source.allMethods(
        "lookup",
        "innerLookup",
        "contextLookup",
        "lookupContext",
        "inputModeContextLookup",
        "childInputModeContextLookup",
        "getCopy",
        "getOrCreateCopy"
    )
        .forEach {
            it.setSingleTypeParameter()

            it.typeParameter.addGeneric("T")

            it.getJSONObject(J_RETURNS)
                .put(J_TYPE, "T")

            it.getJSONArray(J_MODIFIERS)
                .put(CANBENULL)
        }

    source.allMethods("getDecoratorFor")
        .forEach {
            it.firstParameter.addGeneric("TInterface")
        }

    source.allMethods(
        "typedHitElementsAt",
        "createHitTester",

        "serializeCore",
        "deserializeCore"
    )
        .forEach {
            it.firstParameter.addGeneric("T")
        }

    source.allMethods(
        "getCurrent",
        "serialize",
        "deserialize",
        "setLookup"
    )
        .map { it.firstParameter }
        .filter { it.getString(J_TYPE) == YCLASS }
        .forEach {
            it.addGeneric("T")
        }

    source.allMethods("factoryLookupChainLink", "add", "addConstant")
        .filter { it.firstParameter.getString(J_NAME) == "contextType" }
        .forEach {
            it.parameter("contextType").addGeneric("TContext")
            it.parameter("resultType").addGeneric("TResult")
        }

    source.allMethods("addConstant", "ofType")
        .map { it.firstParameter }
        .filter { it.getString(J_NAME) == "resultType" }
        .forEach { it.addGeneric("TResult") }

    source.allMethods(
        "addGraphInputData",
        "addGraphOutputData"
    )
        .forEach {
            it.firstParameter.addGeneric("TValue")
        }

    source.allMethods("addOutputMapper")
        .forEach {
            it.parameter("modelItemType").addGeneric("TModelItem")
            it.parameter("dataType").addGeneric("TValue")
        }

    source.allMethods("addRegistryOutputMapper")
        .filter { it.firstParameter.getString(J_NAME) == "modelItemType" }
        .forEach {
            it.parameter("modelItemType").addGeneric("TModelItem")
            it.parameter("valueType").addGeneric("TValue")
        }

    source.type("GraphMLIOHandler")
        .allMethodParameters()
        .filter { it.getString(J_TYPE) == YCLASS }
        .forEach {
            when (it.getString(J_NAME)) {
                "keyType" -> it.addGeneric("TKey")
                "modelItemType" -> it.addGeneric("TKey")
                "dataType" -> it.addGeneric("TData")
            }
        }

    source.allMethods(
        "addMapper",
        "addConstantMapper",
        "addDelegateMapper",

        "createMapper",
        "createConstantMapper",
        "createDelegateMapper",

        "addDataProvider",
        "createDataMap",
        "createDataProvider"
    )
        .filter { it.firstParameter.getString(J_NAME) == "keyType" }
        .filter { it.secondParameter.getString(J_NAME) == "valueType" }
        .forEach {
            it.parameter("keyType").addGeneric("K")
            it.parameter("valueType").addGeneric("V")
        }
}

private fun addConstructorClassGeneric(source: Source) {
    source.types()
        .forEach { type ->
            val typeName = type.getString(J_NAME)
            type.optionalArray(J_CONSTRUCTORS)
                .optionalArray(J_PARAMETERS)
                .filter { it.getString(J_TYPE) == YCLASS }
                .forEach {
                    val name = it.getString(J_NAME)
                    val generic = when (name) {
                        "edgeStyleType" -> "TStyle"
                        "decoratedType" -> "TDecoratedType"
                        "interfaceType" -> "TInterface"
                        "keyType" ->
                            when (typeName) {
                                "DataMapAdapter" -> "K"
                                "ItemCollectionMapping" -> "TItem"
                                else -> "TKey"
                            }
                        "valueType" -> if (typeName == "DataMapAdapter") "V" else "TValue"
                        "dataType" -> "TData"
                        "itemType" -> "T"
                        "type" -> when (typeName) {
                            "StripeDecorator" -> "TStripe"
                            else -> null
                        }
                        else -> null
                    }

                    if (generic != null) {
                        it.addGeneric(generic)
                    }
                }
        }
}

private fun addMethodClassGeneric(source: Source) {
    source.type("ILookup")
        .staticMethod("createSingleLookup")
        .apply {
            setSingleTypeParameter()
            firstParameter.put(J_TYPE, "T")
            secondParameter.addGeneric("T")
        }
}

private fun addMapperMetadataGeneric(source: Source) {
    val type = source.type("MapperMetadata")

    type.setTypeParameters("TKey", "TValue")

    type.jsequence(J_CONSTRUCTORS)
        .jsequence(J_PARAMETERS)
        .filter { it.getString(J_NAME) == "metadata" }
        .forEach { it.addGeneric("TKey,TValue") }

    type.jsequence(J_PROPERTIES)
        .forEach {
            when (it.getString(J_NAME)) {
                "keyType" -> it.addGeneric("TKey")
                "valueType" -> it.addGeneric("TValue")
            }
        }

    type.staticMethod("create")
        .apply {
            parameter("keyType").addGeneric("TKey")
            parameter("valueType").addGeneric("TValue")

            getJSONObject(J_RETURNS)
                .addGeneric("TKey,TValue")
        }

    source.type("MapperOutputHandler")
        .getJSONArray(J_PROPERTIES)
        .firstWithName("mapperMetadata")
        .addGeneric("TKey,TData")

    source.types(
        "IMapperRegistry",
        "MapperRegistry"
    ).forEach {
        val methods = it.getJSONArray(J_METHODS)
        methods.firstWithName("getMapperMetadata")
            .apply {
                setTypeParameters("K", "V")
                getJSONObject(J_RETURNS)
                    .addGeneric("K,V")
            }

        methods.firstWithName("setMapperMetadata")
            .apply {
                setTypeParameters("K", "V")
                parameter("metadata")
                    .addGeneric("K,V")
            }
    }
}

private fun removeUnusedTypeParameters(source: Source) {
    source.allMethods("removeLookup")
        .forEach { it.remove(J_TYPE_PARAMETERS) }
}

private fun addClassBounds(source: Source) {
    val typeNames = setOf(
        "TModelItem",
        "TItem"
    )

    source.types()
        .forEach { type ->
            type.optJsequence(J_TYPE_PARAMETERS)
                .filter { it.getString(J_NAME) in typeNames }
                .forEach {
                    val bound = when (type.getString(J_ID)) {
                        "yfiles.graph.ItemChangedEventArgs" -> "yfiles.graph.ITagOwner"
                        else -> IMODEL_ITEM
                    }
                    it.put(J_BOUNDS, arrayOf(bound))
                }
        }

    source.types(
        "DpKeyItemCollection",

        "ItemTappedEventArgs",
        "TableItemTappedEventArgs",

        "IGridConstraintProvider",
        "GridConstraintProvider",

        "IHitTester",

        "ItemDropInputMode",

        "ISelectionModel",
        "DefaultSelectionModel",

        // replace mode
        "HighlightIndicatorManager",
        "SelectionIndicatorManager"
    ).map { it.jsequence(J_TYPE_PARAMETERS).single() }
        .forEach { it.put(J_BOUNDS, arrayOf(IMODEL_ITEM)) }

    source.type("GraphModelManager")
        .jsequence(J_METHODS)
        .first { it.getString(J_NAME) == "createHitTester" }
        .jsequence(J_TYPE_PARAMETERS)
        .single()
        .put(J_BOUNDS, arrayOf(IMODEL_ITEM))
}

private fun addTypeParameterBounds(source: Source) {
    source.types()
        .filter { it.has(J_TYPE_PARAMETERS) }
        .filter { it.has(J_CONSTRUCTORS) }
        .forEach {
            val boundMap = it.jsequence(J_CONSTRUCTORS)
                .filter { it.has(J_PARAMETERS) }
                .jsequence(J_PARAMETERS)
                .mapNotNull { it.classBoundPair }
                .toMap()

            if (boundMap.isNotEmpty()) {
                it.jsequence(J_TYPE_PARAMETERS)
                    .filter { !it.has(J_BOUNDS) }
                    .forEach {
                        val name = it.getString(J_NAME)
                        val bound = boundMap.get(name)
                        if (bound != null) {
                            it.put(J_BOUNDS, arrayOf(bound))
                        }
                    }
            }
        }

    source.types()
        .flatMap { it.optJsequence(J_METHODS) + it.optJsequence(J_STATIC_METHODS) }
        .filter { it.has(J_TYPE_PARAMETERS) }
        .forEach {
            val boundMap = it.jsequence(J_PARAMETERS)
                .mapNotNull { it.classBoundPair }
                .toMap()

            if (boundMap.isNotEmpty()) {
                it.jsequence(J_TYPE_PARAMETERS)
                    .filter { !it.has(J_BOUNDS) }
                    .forEach {
                        val name = it.getString(J_NAME)
                        val bound = boundMap.get(name)
                        if (bound != null) {
                            it.put(J_BOUNDS, arrayOf(bound))
                        }
                    }
            }
        }

    source.types(
        "IMapperRegistry",
        "MapperRegistry"
    ).jsequence(J_METHODS)
        .filter { it.getString(J_NAME).contains("Metadata") }
        .jsequence(J_TYPE_PARAMETERS)
        .forEach {
            it.put(J_BOUNDS, arrayOf(ANY))
        }
}

private val JSONObject.classBoundPair: Pair<String, String>?
    get() {
        val type = getString(J_TYPE)
        if (type.startsWith("$YCLASS<")) {
            val generic = between(type, "$YCLASS<", ">")
            if (generic.contains(".")) {
                return null
            }

            val bound = when {
                generic == "TModelItem" -> IMODEL_ITEM
                generic == "TDecoratedType" -> IMODEL_ITEM
                getString(J_NAME) == "modelItemType" -> IMODEL_ITEM
                else -> ANY
            }

            return generic to bound
        }

        if (type.contains("DpKey<")) {
            val generic = between(type, "DpKey<", ">")
            if (generic.contains(".")) {
                return null
            }

            return generic to ANY
        }

        return null
    }