package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import org.json.JSONObject
import java.io.File

internal fun generateClassUtils(moduleName: String, sourceDir: File) {
    sourceDir.resolve("yfiles/lang/BaseClass.kt")
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

    sourceDir.resolve("yfiles/lang/ClassMetadata.kt")
        .writeText(
            // language=kotlin
            """
                |@file:Suppress("NOTHING_TO_INLINE")
                |
                |package yfiles.lang
                |
                |external interface TypeMetadata<T: Any>
                |
                |external interface ClassMetadata<T: Any> : TypeMetadata<T>
                |
                |external interface InterfaceMetadata<T: Any>: TypeMetadata<T>
                |
                |inline val <T: Any> TypeMetadata<T>.yclass:Class<T>
                |    get() = asDynamic()["\${'$'}class"]
                |    
                |inline fun <T: Any> TypeMetadata<T>.isInstance(o:Any):Boolean = 
                |    asDynamic().isInstance(o)
                |
                |inline infix fun <T : Any> Any.yIs(clazz: InterfaceMetadata<T>): Boolean =
                |    clazz.isInstance(this)
                |
                |inline infix fun <T : Any> Any?.yIs(clazz: InterfaceMetadata<T>): Boolean =
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

        get(J_METHODS)
            .firstWithName("newInstance")
            .get(J_RETURNS)
            .put(J_TYPE, "T")

        get(J_STATIC_METHODS).apply {
            removeAll { true }

            put(
                mapOf(
                    J_NAME to "fixType",
                    J_MODIFIERS to listOf(STATIC, HIDDEN),
                    J_PARAMETERS to listOf(
                        mapOf(
                            J_NAME to "type",
                            J_TYPE to "$JS_CLASS<out $YOBJECT_CLASS_ALIAS>"
                        ),
                        mapOf(
                            J_NAME to "name",
                            J_TYPE to JS_STRING,
                            J_MODIFIERS to listOf(OPTIONAL)
                        )
                    )
                )
            )
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

            it[J_RETURNS]
                .put(J_TYPE, "T")

            it[J_MODIFIERS]
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
        .filter { it[J_TYPE] == YCLASS }
        .forEach {
            it.addGeneric("T")
        }

    source.allMethods("factoryLookupChainLink", "add", "addConstant")
        .filter { it.firstParameter[J_NAME] == "contextType" }
        .forEach {
            it.parameter("contextType").addGeneric("TContext")
            it.parameter("resultType").addGeneric("TResult")
        }

    source.allMethods("addConstant", "ofType")
        .map { it.firstParameter }
        .filter { it[J_NAME] == "resultType" }
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
        .filter { it.firstParameter[J_NAME] == "modelItemType" }
        .forEach {
            it.parameter("modelItemType").addGeneric("TModelItem")
            it.parameter("valueType").addGeneric("TValue")
        }

    source.type("GraphMLIOHandler")
        .allMethodParameters()
        .filter { it[J_TYPE] == YCLASS }
        .forEach {
            when (it[J_NAME]) {
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
        .filter { it.firstParameter[J_NAME] == "keyType" }
        .filter { it.secondParameter[J_NAME] == "valueType" }
        .forEach {
            it.parameter("keyType").addGeneric("K")
            it.parameter("valueType").addGeneric("V")
        }
}

private fun addConstructorClassGeneric(source: Source) {
    source.types()
        .forEach { type ->
            val typeName = type[J_NAME]
            type.optionalArray(J_CONSTRUCTORS)
                .optionalArray(J_PARAMETERS)
                .filter { it[J_TYPE] == YCLASS }
                .forEach {
                    val name = it[J_NAME]
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
        .filter { it[J_NAME] == "metadata" }
        .forEach { it.addGeneric("TKey,TValue") }

    type.jsequence(J_PROPERTIES)
        .forEach {
            when (it[J_NAME]) {
                "keyType" -> it.addGeneric("TKey")
                "valueType" -> it.addGeneric("TValue")
            }
        }

    type.staticMethod("create")
        .apply {
            parameter("keyType").addGeneric("TKey")
            parameter("valueType").addGeneric("TValue")

            get(J_RETURNS)
                .addGeneric("TKey,TValue")
        }

    source.type("MapperOutputHandler")
        .get(J_PROPERTIES)
        .firstWithName("mapperMetadata")
        .addGeneric("TKey,TData")

    source.types(
        "IMapperRegistry",
        "MapperRegistry"
    ).forEach {
        val methods = it[J_METHODS]
        methods.firstWithName("getMapperMetadata")
            .apply {
                setTypeParameters("K", "V")
                get(J_RETURNS)
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
                .filter { it[J_NAME] in typeNames }
                .forEach {
                    val bound = when (type[J_ID]) {
                        "yfiles.graph.ItemChangedEventArgs" -> "yfiles.graph.ITagOwner"
                        else -> IMODEL_ITEM
                    }
                    it.put(J_BOUNDS, arrayOf(bound))
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
    ).map { it.jsequence(J_TYPE_PARAMETERS).single() }
        .forEach { it.put(J_BOUNDS, arrayOf(IMODEL_ITEM)) }

    source.type("ResultItemMapping")
        .jsequence(J_TYPE_PARAMETERS)
        .first()
        .put(J_BOUNDS, arrayOf(IMODEL_ITEM))

    source.type("GraphModelManager")
        .jsequence(J_METHODS)
        .first { it[J_NAME] == "createHitTester" }
        .jsequence(J_TYPE_PARAMETERS)
        .single()
        .put(J_BOUNDS, arrayOf(IMODEL_ITEM))

    source.types(
        "ResultItemCollection",

        "IObservableCollection",
        "ObservableCollection",

        "DelegateUndoUnit",
        "ItemCopiedEventArgs",

        "Future"
    ).map { it.jsequence(J_TYPE_PARAMETERS).single() }
        .forEach { it.put(J_BOUNDS, arrayOf(JS_OBJECT)) }

    source.types(
        "ResultItemMapping",
        "GraphBuilderItemEventArgs",
        "ItemChangedEventArgs"
    ).map { it[J_TYPE_PARAMETERS].get(1) as JSONObject }
        .forEach { it.put(J_BOUNDS, arrayOf(JS_OBJECT)) }
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
                        val name = it[J_NAME]
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
                        val name = it[J_NAME]
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
        .filter { it[J_NAME].contains("Metadata") }
        .jsequence(J_TYPE_PARAMETERS)
        .forEach {
            it.put(J_BOUNDS, arrayOf(JS_OBJECT))
        }
}

private val JSONObject.classBoundPair: Pair<String, String>?
    get() {
        val type = get(J_TYPE)
        if (type.startsWith("$YCLASS<")) {
            val generic = between(type, "$YCLASS<", ">")
            if (generic.contains(".")) {
                return null
            }

            val bound = when {
                generic == "TModelItem" -> IMODEL_ITEM
                generic == "TDecoratedType" -> IMODEL_ITEM
                get(J_NAME) == "modelItemType" -> IMODEL_ITEM
                else -> JS_OBJECT
            }

            return generic to bound
        }

        if (type.contains("DpKey<")) {
            val generic = between(type, "DpKey<", ">")
            if (generic.contains(".")) {
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
    ).map { it.jsequence(J_TYPE_PARAMETERS).first() }
        .forEach { it.put(J_BOUNDS, arrayOf(JS_OBJECT)) }

    source.types()
        .flatMap { it.optJsequence(J_METHODS) + it.optJsequence(J_STATIC_METHODS) }
        .filter { it.has(J_TYPE_PARAMETERS) }
        .map { it.jsequence(J_TYPE_PARAMETERS).first() }
        .filterNot { it.has(J_BOUNDS) }
        .filter { it[J_NAME] == "K" }
        .forEach { it.put(J_BOUNDS, arrayOf(JS_OBJECT)) }
}
