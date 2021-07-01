package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.ContentMode.*
import com.github.turansky.yfiles.json.get
import com.github.turansky.yfiles.json.jArray
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.removeAllObjects
import com.github.turansky.yfiles.json.removeItem
import org.json.JSONObject

internal fun generateClassUtils(context: GeneratorContext) {
    // language=kotlin
    context[BASE_CLASS, CLASS] =
        """
            $HIDDEN_METHOD_ANNOTATION
            external fun BaseClass(vararg types: JsClass<out $YOBJECT>):JsClass<out $YOBJECT>

            $HIDDEN_METHOD_ANNOTATION
            inline fun callSuperConstructor(o: $YOBJECT) {
               o.$AS_DYNAMIC.__proto__.__proto__.constructor.call(o)
            }
        """.trimIndent()

    val primitiveTypeMetadata = sequenceOf(
        BOOLEAN to "__BOOLEAN__",
        DOUBLE to "__NUMBER__",
        INT to "__NUMBER__",
        STRING to "__STRING__"
    ).joinToString("\n\n") { (type, alias) ->
        """
            inline val $type.Companion.yclass: $YCLASS<$type>
                get() = $alias.unsafeCast<$ICLASS_METADATA<$type>>().yclass
        """.trimIndent()
    }

    // language=kotlin
    context[ICLASS_METADATA] =
        """
            private const val YCLASS = "\${'$'}class"
            
            external interface IClassMetadata<T: Any> {
               @JsName(YCLASS)
               val yclass:$YCLASS<T>   
            }
            
            inline val <T: $YOBJECT> JsClass<T>.yclass:$YCLASS<T>
                get() = unsafeCast<IClassMetadata<T>>().yclass
            
            internal fun <T: $ANY> JsClass<T>.findClass():$YCLASS<T>? =
                $AS_DYNAMIC[YCLASS] as? $YCLASS<T>
            
            $primitiveTypeMetadata    
        """.trimIndent()

    // language=kotlin
    context[ICLASS_METADATA, DELEGATE] =
        """
            inline fun <reified T: $YOBJECT> classMetadata(): $ICLASS_METADATA<T> = 
                classMetadata(T::class.js.yclass)
                
            fun <T: $YOBJECT> classMetadata(yclass: $YCLASS<T>): $ICLASS_METADATA<T> = 
                SimpleClassMetadata(yclass)    
                
            private class SimpleClassMetadata<T: $YOBJECT>(
                override val yclass: $YCLASS<T>
            ): $ICLASS_METADATA<T> {
                // WA: https://youtrack.jetbrains.com/issue/KT-40155
                @JsName("yclass")
                private val yclassDelegate: $YCLASS<T> = yclass
            }
        """.trimIndent()

    // language=kotlin
    context[CLASS_METADATA] = """
        @JsName("Object")
        abstract external class ClassMetadata<T: $YOBJECT> 
        internal constructor() : $ICLASS_METADATA<T> {
            override val yclass: $YCLASS<T>
        }
    """.trimIndent()

    // language=kotlin
    context[ENUM_METADATA] = """
        @JsName("Object")
        abstract external class EnumMetadata<T: $YENUM<T>> 
        internal constructor() : $ICLASS_METADATA<T> {
            override val yclass: $YCLASS<T>
        }
    """.trimIndent()

    // language=kotlin
    context[INTERFACE_METADATA] = """
        @JsName("Object")
        abstract external class InterfaceMetadata<T: $YOBJECT>
        internal constructor() : $ICLASS_METADATA<T> {
           override val yclass: $YCLASS<T>
        }
        
        inline fun jsInstanceOf(
           o: Any?, 
           type: $INTERFACE_METADATA<*>
        ): Boolean =
            type.$AS_DYNAMIC.isInstance(o)
        
        inline infix fun Any?.yIs(type: $INTERFACE_METADATA<*>): Boolean =
            jsInstanceOf(this, type)
        
        inline infix fun <T : $YOBJECT> Any?.yOpt(type: $INTERFACE_METADATA<T>): T? =
            if (yIs(type)) {
                unsafeCast<T>()
            } else {
                null
            }
        
        inline infix fun <T : $YOBJECT> Any?.yAs(type: $INTERFACE_METADATA<T>): T {
           require(this yIs type)
        
           return unsafeCast<T>()
        }
    """.trimIndent()

    // language=kotlin
    context[YENUM, EXTENSIONS] = """
        $HIDDEN_METHOD_ANNOTATION
        inline fun <T: $YENUM<T>> getEnumName(
            value: T, 
            type: EnumMetadata<T>
        ): String =
            YEnum.getName(type.yclass, value)
 
        $HIDDEN_METHOD_ANNOTATION
        inline fun <T: $YENUM<T>> getEnumValues(
            type: EnumMetadata<T>
        ): Array<out T> =
            YEnum.getValues(type.yclass)
                
        $HIDDEN_METHOD_ANNOTATION
        inline fun <T: $YENUM<T>> getEnumValueOf(
            type: EnumMetadata<T>,
            id: String
        ): T =
            YEnum.parse(type.yclass, id, true)       
    """.trimIndent()
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
    source.type("Class") {
        setSingleTypeParameter(bound = JS_OBJECT)

        get(MODIFIERS).put(FINAL)

        this[CONSTRUCTORS] = jArray(
            jObject(
                MODIFIERS to arrayOf(PRIVATE)
            )
        )

        get(METHODS).removeItem(method("getProperties"))

        method("newInstance")
            .get(RETURNS)
            .set(TYPE, "T")

        get(METHODS).apply {
            removeAllObjects {
                STATIC in it[MODIFIERS] && it[NAME] != "fixType"
            }

            get("fixType").apply {
                firstParameter[TYPE] = "$JS_CLASS<out $YOBJECT>"
                get(MODIFIERS).put(HIDDEN)
            }
        }
    }
}

private fun fixEnum(source: Source) {
    source.type("Enum") {
        set(ID, YENUM)
        set(NAME, "YEnum")
        set(ES6_NAME, "Enum")
        set(GROUP, "interface")
        setSingleTypeParameter(bound = "$YENUM<T>")

        flatMap(METHODS)
            .onEach { it.setSingleTypeParameter(bound = "$YENUM<T>") }
            .onEach {
                val returns = it[RETURNS]
                when (returns[TYPE]) {
                    JS_NUMBER -> returns[TYPE] = "T"
                    "Array<$JS_NUMBER>" -> returns[TYPE] = "Array<T>"
                }
            }
            .flatMap(PARAMETERS)
            .forEach {
                when (it[TYPE]) {
                    YCLASS -> it.addGeneric("T")
                    JS_NUMBER -> it[TYPE] = "$YENUM<T>"
                }
            }
    }

    source.type("Direction")
        .get(MODIFIERS)
        .put(ENUM_LIKE)
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
            it.setSingleTypeParameter(bound = YOBJECT)

            it.typeParameter.addGeneric("T")

            it[RETURNS][TYPE] = "T"

            it[MODIFIERS]
                .put(CANBENULL)
        }

    source.allMethods("getDecoratorFor").forEach {
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
            type.optFlatMap(CONSTRUCTORS)
                .optFlatMap(PARAMETERS)
                .filter { it[TYPE] == YCLASS }
                .forEach {
                    val generic = when (it[NAME]) {
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
        .method("createSingleLookup")
        .apply {
            setSingleTypeParameter(bound = YOBJECT)
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

    type.method("create")
        .apply {
            parameter("keyType").addGeneric("TKey")
            parameter("valueType").addGeneric("TValue")

            get(RETURNS)
                .addGeneric("TKey,TValue")
        }

    source.type("MapperOutputHandler")
        .property("mapperMetadata")
        .addGeneric("TKey,TData")

    source.types(
        "IMapperRegistry",
        "MapperRegistry"
    ).forEach {
        it.method("getMapperMetadata").apply {
            setTypeParameters("K", "V")
            get(RETURNS)
                .addGeneric("K,V")
        }

        it.method("setMapperMetadata").apply {
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
                        "yfiles.graph.ItemChangedEventArgs" -> ITAG_OWNER
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
        .filter { it[NAME] == "createHitTester" || it[NAME] == "typedHitElementsAt" }
        .flatMap(TYPE_PARAMETERS)
        .forEach { it[BOUNDS] = arrayOf(IMODEL_ITEM) }

    source.types(
        "DelegateUndoUnit",
        "ItemCopiedEventArgs"
    ).map { it.flatMap(TYPE_PARAMETERS).single() }
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }

    source.types(
        "ResultItemCollection",

        "IObservableCollection",
        "ObservableCollection",

        "Future"
    ).map { it.flatMap(TYPE_PARAMETERS).single() }
        .forEach { it[BOUNDS] = arrayOf(YOBJECT) }

    source.type("ItemEventArgs")
        .flatMap(TYPE_PARAMETERS)
        .single()[BOUNDS] = arrayOf("$YOBJECT?")

    source.type("HoveredItemChangedEventArgs") {
        val validExtends = get(EXTENDS)
            .replace("<$IMODEL_ITEM>", "<$IMODEL_ITEM?>")

        set(EXTENDS, validExtends)
    }

    source.types(
        "ResultItemMapping",
        "ItemChangedEventArgs"
    ).map { it[TYPE_PARAMETERS].getJSONObject(1) }
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
        .optFlatMap(METHODS)
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
            val generic = type.between("$YCLASS<", ">")
            if ("." in generic) {
                return null
            }

            val bound = when {
                generic == "TInterface" -> YOBJECT
                generic == "TContext" -> YOBJECT
                generic == "TResult" -> YOBJECT
                generic == "TModelItem" -> IMODEL_ITEM
                generic == "TDecoratedType" -> IMODEL_ITEM
                get(NAME) == "modelItemType" -> IMODEL_ITEM
                else -> JS_OBJECT
            }

            return generic to bound
        }

        if ("DpKey<" in type) {
            val generic = type.between("DpKey<", ">")
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
        .optFlatMap(METHODS)
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
