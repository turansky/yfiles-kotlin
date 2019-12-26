package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

internal fun generateClassUtils(moduleName: String, context: GeneratorContext) {
    context.resolve("yfiles/lang/BaseClass.kt")
        .writeText(
            // language=kotlin
            """
                |@file:JsModule("$moduleName") 
                |package yfiles.lang
                |
                |$HIDDEN_METHOD_ANNOTATION
                |external fun BaseClass(vararg types: JsClass<*>):JsClass<out YObject>
            """.trimMargin()
        )

    context.resolve("yfiles/lang/ClassMetadata.kt")
        .writeText(
            // language=kotlin
            """
                |@file:Suppress("NOTHING_TO_INLINE")
                |
                |package yfiles.lang
                |
                |external interface TypeMetadata<T: Any>
                |
                |inline val <T: Any> TypeMetadata<T>.yclass:YClass<T>
                |    get() = asDynamic()["\${'$'}class"]
                |    
                |external interface ClassMetadata<T: Any> : TypeMetadata<T>
                |
                |external interface InterfaceMetadata<T: Any>: TypeMetadata<T>
                |    
                |inline fun <T: Any> InterfaceMetadata<T>.isInstance(o:Any):Boolean = 
                |    asDynamic().isInstance(o)
                |
                |inline infix fun Any.yIs(clazz: InterfaceMetadata<*>): Boolean =
                |    clazz.isInstance(this)
                |
                |inline infix fun Any?.yIs(clazz: InterfaceMetadata<*>): Boolean =
                |    this != null && this yIs clazz
                |
                |inline infix fun <T : Any> Any.yOpt(clazz: InterfaceMetadata<T>): T? =
                |    if (this yIs clazz) {
                |        unsafeCast<T>()
                |    } else {
                |        null
                |    }
                |
                |inline infix fun <T : Any> Any?.yOpt(clazz: InterfaceMetadata<T>): T? {
                |    this ?: return null
                |
                |    return this yOpt clazz
                |}
                |
                |inline infix fun <T : Any> Any.yAs(clazz: InterfaceMetadata<T>): T {
                |    require(this yIs clazz)
                |
                |    return unsafeCast<T>()
                |}
                |
                |inline infix fun <T : Any> Any?.yAs(clazz: InterfaceMetadata<T>): T =
                |    requireNotNull(this) yAs clazz
            """.trimMargin()
        )
}

internal fun applyClassHacks(source: Source) {
    fixClass(source)
    fixEnum(source)

    addClassGeneric(source)
    addConstructorClassGeneric(source)
    addMethodClassGeneric(source)
    addMapperMetadataGeneric(source)

    removeUnusedTypeParameters(source)
    addClassBounds(source)

    addTypeParameterBounds(source)
    addMapClassBounds(source)
}

private fun fixClass(source: Source) {
    source.type("Class").apply {
        setSingleTypeParameter(bound = JS_OBJECT)

        get(METHODS)
            .get("newInstance")
            .get(RETURNS)
            .set(TYPE, "T")

        get(STATIC_METHODS).apply {
            removeAll { true }

            put(
                mapOf(
                    NAME to "fixType",
                    MODIFIERS to listOf(STATIC, HIDDEN),
                    PARAMETERS to listOf(
                        mapOf(
                            NAME to "type",
                            TYPE to "$JS_CLASS<out $YOBJECT>"
                        ),
                        mapOf(
                            NAME to "name",
                            TYPE to JS_STRING,
                            MODIFIERS to listOf(OPTIONAL)
                        )
                    )
                )
            )
        }
    }
}

private fun fixEnum(source: Source) {
    val ENUM = "yfiles.lang.Enum"

    source.type("Enum").apply {
        set(ID, YENUM)
        set(NAME, "YEnum")
        set(ES6_NAME, "Enum")
        set(GROUP, "interface")
        setSingleTypeParameter(bound = "$YENUM<T>")

        flatMap(STATIC_METHODS)
            .onEach { it.setSingleTypeParameter(bound = "$YENUM<T>") }
            .onEach {
                val returns = it[RETURNS]
                when (returns[TYPE]) {
                    ENUM -> returns[TYPE] = "$YENUM<T>"
                    "Array<$JS_NUMBER>" -> returns[TYPE] = "Array<$YENUM<T>>"
                }
            }
            .flatMap(PARAMETERS)
            .forEach {
                when (it[TYPE]) {
                    YCLASS -> it.addGeneric("T")
                    ENUM -> it[TYPE] = "$YENUM<T>"
                }
            }
    }
}

private fun addClassGeneric(source: Source) {
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

            it[RETURNS][TYPE] = "T"

            it[MODIFIERS]
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
        .filter { it[TYPE] == YCLASS }
        .forEach {
            it.addGeneric("T")
        }

    source.allMethods("factoryLookupChainLink", "add", "addConstant")
        .filter { it.firstParameter[NAME] == "contextType" }
        .forEach {
            it.parameter("contextType").addGeneric("TContext")
            it.parameter("resultType").addGeneric("TResult")
        }

    source.allMethods("addConstant", "ofType")
        .map { it.firstParameter }
        .filter { it[NAME] == "resultType" }
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
        .filter { it.firstParameter[NAME] == "modelItemType" }
        .forEach {
            it.parameter("modelItemType").addGeneric("TModelItem")
            it.parameter("valueType").addGeneric("TValue")
        }

    source.type("GraphMLIOHandler")
        .allMethodParameters()
        .filter { it[TYPE] == YCLASS }
        .forEach {
            when (it[NAME]) {
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
        .filter { it.firstParameter[NAME] == "keyType" }
        .filter { it.secondParameter[NAME] == "valueType" }
        .forEach {
            it.parameter("keyType").addGeneric("K")
            it.parameter("valueType").addGeneric("V")
        }
}

private fun addConstructorClassGeneric(source: Source) {
    source.types()
        .forEach { type ->
            val typeName = type[NAME]
            type.optionalArray(CONSTRUCTORS)
                .optFlatMap(PARAMETERS)
                .filter { it[TYPE] == YCLASS }
                .forEach {
                    val name = it[NAME]
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
            firstParameter[TYPE] = "T"
            secondParameter.addGeneric("T")
        }
}

private fun addMapperMetadataGeneric(source: Source) {
    val type = source.type("MapperMetadata")

    type.setTypeParameters("TKey", "TValue")

    type.flatMap(CONSTRUCTORS)
        .flatMap(PARAMETERS)
        .filter { it[NAME] == "metadata" }
        .forEach { it.addGeneric("TKey,TValue") }

    type.flatMap(PROPERTIES)
        .forEach {
            when (it[NAME]) {
                "keyType" -> it.addGeneric("TKey")
                "valueType" -> it.addGeneric("TValue")
            }
        }

    type.staticMethod("create")
        .apply {
            parameter("keyType").addGeneric("TKey")
            parameter("valueType").addGeneric("TValue")

            get(RETURNS)
                .addGeneric("TKey,TValue")
        }

    source.type("MapperOutputHandler")
        .get(PROPERTIES)
        .get("mapperMetadata")
        .addGeneric("TKey,TData")

    source.types(
        "IMapperRegistry",
        "MapperRegistry"
    ).forEach {
        val methods = it[METHODS]
        methods["getMapperMetadata"]
            .apply {
                setTypeParameters("K", "V")
                get(RETURNS)
                    .addGeneric("K,V")
            }

        methods["setMapperMetadata"]
            .apply {
                setTypeParameters("K", "V")
                parameter("metadata")
                    .addGeneric("K,V")
            }
    }
}

private fun removeUnusedTypeParameters(source: Source) {
    source.allMethods("removeLookup")
        .forEach { it.remove(TYPE_PARAMETERS) }
}

private fun addClassBounds(source: Source) {
    val typeNames = setOf(
        "TModelItem",
        "TItem"
    )

    source.types()
        .forEach { type ->
            type.optFlatMap(TYPE_PARAMETERS)
                .filter { it[NAME] in typeNames }
                .forEach {
                    val bound = when (type[ID]) {
                        "yfiles.graph.ItemChangedEventArgs" -> "yfiles.graph.ITagOwner"
                        else -> IMODEL_ITEM
                    }
                    it[BOUNDS] = arrayOf(bound)
                }
        }

    source.types(
        "DpKeyItemCollection",

        "ItemClickedEventArgs",
        "TableItemClickedEventArgs",

        "ItemTappedEventArgs",
        "TableItemTappedEventArgs",

        "IGridConstraintProvider",
        "GridConstraintProvider",

        "IHitTester",

        "ItemDropInputMode",

        "ISelectionModel",
        "DefaultSelectionModel",

        "ModelManager",

        // replace mode
        "SelectionIndicatorManager",
        "FocusIndicatorManager",
        "HighlightIndicatorManager",

        "ItemSelectionChangedEventArgs"
    ).map { it.flatMap(TYPE_PARAMETERS).single() }
        .forEach { it[BOUNDS] = arrayOf(IMODEL_ITEM) }

    source.type("ResultItemMapping")
        .flatMap(TYPE_PARAMETERS)
        .first()
        .set(BOUNDS, arrayOf(IMODEL_ITEM))

    source.type("GraphModelManager")
        .flatMap(METHODS)
        .first { it[NAME] == "createHitTester" }
        .flatMap(TYPE_PARAMETERS)
        .single()
        .set(BOUNDS, arrayOf(IMODEL_ITEM))

    source.types(
        "ResultItemCollection",

        "IObservableCollection",
        "ObservableCollection",

        "DelegateUndoUnit",
        "ItemCopiedEventArgs",

        "Future"
    ).map { it.flatMap(TYPE_PARAMETERS).single() }
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }

    source.types(
        "ResultItemMapping",
        "GraphBuilderItemEventArgs",
        "ItemChangedEventArgs"
    ).map { it[TYPE_PARAMETERS].get(1) as JSONObject }
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }
}

private fun addTypeParameterBounds(source: Source) {
    source.types()
        .filter { it.has(TYPE_PARAMETERS) }
        .filter { it.has(CONSTRUCTORS) }
        .forEach {
            val boundMap = it.flatMap(CONSTRUCTORS)
                .optFlatMap(PARAMETERS)
                .mapNotNull { it.classBoundPair }
                .toMap()

            if (boundMap.isNotEmpty()) {
                it.flatMap(TYPE_PARAMETERS)
                    .filter { !it.has(BOUNDS) }
                    .forEach {
                        val bound = boundMap[it[NAME]]
                        if (bound != null) {
                            it[BOUNDS] = arrayOf(bound)
                        }
                    }
            }
        }

    source.types()
        .flatMap { it.optFlatMap(METHODS) + it.optFlatMap(STATIC_METHODS) }
        .filter { it.has(TYPE_PARAMETERS) }
        .forEach {
            val boundMap = it.flatMap(PARAMETERS)
                .mapNotNull { it.classBoundPair }
                .toMap()

            if (boundMap.isNotEmpty()) {
                it.flatMap(TYPE_PARAMETERS)
                    .filter { !it.has(BOUNDS) }
                    .forEach {
                        val name = it[NAME]
                        val bound = boundMap.get(name)
                        if (bound != null) {
                            it[BOUNDS] = arrayOf(bound)
                        }
                    }
            }
        }

    source.types(
        "IMapperRegistry",
        "MapperRegistry"
    ).flatMap(METHODS)
        .filter { "Metadata" in it[NAME] }
        .flatMap(TYPE_PARAMETERS)
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }
}

private val JSONObject.classBoundPair: Pair<String, String>?
    get() {
        val type = get(TYPE)
        if (type.startsWith("$YCLASS<")) {
            val generic = between(type, "$YCLASS<", ">")
            if ("." in generic) {
                return null
            }

            val bound = when {
                generic == "TModelItem" -> IMODEL_ITEM
                generic == "TDecoratedType" -> IMODEL_ITEM
                get(NAME) == "modelItemType" -> IMODEL_ITEM
                else -> JS_OBJECT
            }

            return generic to bound
        }

        if ("DpKey<" in type) {
            val generic = between(type, "DpKey<", ">")
            if ("." in generic) {
                return null
            }

            return generic to JS_OBJECT
        }

        return null
    }

private fun addMapClassBounds(source: Source) {
    source.types(
        "MapEntry",

        "IMap",
        "HashMap",

        "IMapper",
        "Mapper"
    ).map { it.flatMap(TYPE_PARAMETERS).first() }
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }

    source.types()
        .flatMap { it.optFlatMap(METHODS) + it.optFlatMap(STATIC_METHODS) }
        .filter { it.has(TYPE_PARAMETERS) }
        .map { it.flatMap(TYPE_PARAMETERS).first() }
        .filterNot { it.has(BOUNDS) }
        .filter { it[NAME] == "K" }
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }

    source.types(
        "IMapperRegistry",
        "MapperRegistry"
    ).flatMap(METHODS)
        .filter { it[NAME] == "getMapper" }
        .flatMap(TYPE_PARAMETERS)
        .filter { it[NAME] == "V" }
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }
}
