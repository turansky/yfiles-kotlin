package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.ContentMode.DELEGATE
import org.json.JSONObject

internal fun generateClassUtils(context: GeneratorContext) {
    // language=kotlin
    context[CLASS_METADATA] = """
        @JsName("Object")
        abstract external class ClassMetadata<T> internal constructor() : $ICLASS_METADATA<T>

        inline fun jsInstanceOf(
           o: Any?, 
           type: $CLASS_METADATA<*>
        ): Boolean =
            type.$AS_DYNAMIC.isInstance(o)
        
        inline infix fun Any?.yIs(type: $CLASS_METADATA<*>): Boolean =
            jsInstanceOf(this, type)
        
        inline infix fun <T : $ANY> Any?.yOpt(type: $CLASS_METADATA<T>): T? =
            if (yIs(type)) {
                unsafeCast<T>()
            } else {
                null
            }
        
        inline infix fun <T : $ANY> Any?.yAs(type: $CLASS_METADATA<T>): T {
           require(this yIs type)
        
           return unsafeCast<T>()
        }
    """.trimIndent()

    // language=kotlin
    context[INTERFACE_METADATA] = """
        @JsName("Object")
        abstract external class InterfaceMetadata<T> internal constructor() : $ICLASS_METADATA<T>
        
        inline fun jsInstanceOf(
           o: Any?, 
           type: $INTERFACE_METADATA<*>
        ): Boolean =
            type.$AS_DYNAMIC.isInstance(o)
        
        inline infix fun Any?.yIs(type: $INTERFACE_METADATA<*>): Boolean =
            jsInstanceOf(this, type)
        
        inline infix fun <T : $ANY> Any?.yOpt(type: $INTERFACE_METADATA<T>): T? =
            if (yIs(type)) {
                unsafeCast<T>()
            } else {
                null
            }
        
        inline infix fun <T : $ANY> Any?.yAs(type: $INTERFACE_METADATA<T>): T {
           require(this yIs type)
        
           return unsafeCast<T>()
        }
    """.trimIndent()

    // language=kotlin
    context[ICLASS_METADATA] =
        """
            external interface IClassMetadata<T>
        """.trimIndent()
}

internal fun applyClassHacks(source: Source) {
    fixConstructorUsage(source)
    addClassGeneric(source)
    fixLookupGeneric(source)

    addClassBounds(source)
    removeTypeOf(source)
    fixDisposeVisualCallback(source)
}

private fun removeTypeOf(source: Source) {
    source.types()
        .filter { it.has(METHODS) }
        .flatMap(METHODS)
        .filter { it.has(PARAMETERS) }
        .flatMap(PARAMETERS)
        .filter { "typeof" in it[TYPE] }
        .forEach { it[TYPE] = it[TYPE].removePrefix("typeof ") }
}

private fun fixConstructorUsage(source: Source) {
    source.types()
        .filter { it.has(METHODS) }
        .flatMap(METHODS)
        .filter { it.has(PARAMETERS) }
        .flatMap(PARAMETERS)
        .forEach {
            it.convertConstructor()
        }

    source.types()
        .filter { it.has(METHODS) }
        .flatMap(METHODS)
        .filter { it.has(RETURNS) }
        .forEach {
            it[RETURNS].convertConstructor()
        }

    source.types()
        .filter { it.has(CONSTRUCTORS) }
        .flatMap(CONSTRUCTORS)
        .filter { it.has(PARAMETERS) }
        .flatMap(PARAMETERS)
        .forEach {
            it.convertConstructor()
        }

    source.types()
        .filter { it.has(PROPERTIES) }
        .flatMap(PROPERTIES)
        .forEach {
            it.convertConstructor()
        }

    source.functionSignature("yfiles.collections.ContextLookupDelegate")
        .flatMap(PARAMETERS)
        .forEach {
            it.convertConstructor()
        }
}

private val CONSTRUCTOR_REGEX = Regex("Constructor(?:<(\\w+)>)?")

private fun JSONObject.convertConstructor() {
    if (has(TYPE))
        this[TYPE] = this[TYPE].convertConstructor()
    if (has(SIGNATURE))
        this[SIGNATURE] = this[SIGNATURE].convertConstructor()
}

private fun String.convertConstructor() : String {
    return CONSTRUCTOR_REGEX.replace(this) { match ->
        val typeParameter = match.groupValues[1]
        return@replace if (typeParameter.isNotEmpty()) {
            "$ICLASS_METADATA<$typeParameter>"
        } else {
            "$ICLASS_METADATA<*>"
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
            it[MODIFIERS]
                .put(CANBENULL)
        }
}

private fun fixLookupGeneric(source: Source) {
    source.allMethods("lookup")
        .forEach { method ->
            method.setSingleTypeParameter(bound = JS_ANY)
            method.parameter("type")[TYPE] = "$ICLASS_METADATA<T>"
            method.returnsSequence().forEach { it[TYPE] = "T" }
        }
}

private fun removeUnusedTypeParameters(source: Source) {
    source.allMethods("removeLookup")
        .forEach { it.remove(TYPE_PARAMETERS) }
}

private fun addClassBounds(source: Source) {
    source.types()
        .filter { it.has(TYPE_PARAMETERS) }
        .forEach { type ->
            val methods = type.optFlatMap(CONSTRUCTORS) + type.optFlatMap(METHODS)
            val typeParameters = type.flatMap(TYPE_PARAMETERS)
            typeParameters.forEach { typeParam ->
                val hasJS_CLASSRef = methods.filter { it.has(PARAMETERS) && !it.has(TYPE_PARAMETERS) }
                    .flatMap(PARAMETERS)
                    .any { JS_CLASS in it[TYPE] && typeParam[NAME] in it[TYPE] }

                if (hasJS_CLASSRef) {
                    typeParam[BOUNDS] = arrayOf(JS_OBJECT)
                }
            }
        }

    source.types()
        .optFlatMap(METHODS)
        .filter { it.has(TYPE_PARAMETERS) }
        .forEach { method ->
            val typeParameters = method.flatMap(TYPE_PARAMETERS)
            typeParameters.forEach { typeParam ->
                val hasJS_CLASSRef = method.optFlatMap(PARAMETERS)
                    .any { JS_CLASS in it[TYPE] && typeParam[NAME] in it[TYPE] }

                if (hasJS_CLASSRef) {
                    typeParam[BOUNDS] = arrayOf(JS_OBJECT)
                }
            }
        }

    source.types(

        "ItemClickedEventArgs",
        "TableItemClickedEventArgs",

        "IGridConstraintProvider",
        "GridConstraintProvider",

        "IHitTester",

        "ItemDropInputMode",

        "ModelManager",

        // replace mode
        "SelectionIndicatorManager",
        "FocusIndicatorManager",
        "HighlightIndicatorManager",
    ).filter { it.has(TYPE_PARAMETERS) }
        .map { it.flatMap(TYPE_PARAMETERS).single() }
        .forEach { it[BOUNDS] = arrayOf(IMODEL_ITEM) }

    source.type("ResultItemMapping")
        .flatMap(TYPE_PARAMETERS)
        .first()
        .set(BOUNDS, arrayOf(JS_ANY))

    source.types("GraphModelManager")
        .flatMap(METHODS)
        .filter { it[NAME] == "createHitTester" || it[NAME] == "typedHitElementsAt" }
        .flatMap(TYPE_PARAMETERS)
        .forEach { it[BOUNDS] = arrayOf(IMODEL_ITEM) }

    source.types(
            "ItemCopiedEventArgs"
    ).map { it.flatMap(TYPE_PARAMETERS).single() }
        .forEach { it[BOUNDS] = arrayOf(JS_OBJECT) }

    source.types(
        "ResultItemCollection",

        "IObservableCollection",
        "ObservableCollection",
    ).map { it.flatMap(TYPE_PARAMETERS).single() }
        .forEach {
            it[BOUNDS] = emptyArray<String>()
        }

    source.type("ItemEventArgs")
        .flatMap(TYPE_PARAMETERS)
        .single()[BOUNDS] = arrayOf("Any?")

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

private fun fixDisposeVisualCallback(source: Source) {
    source.functionSignature("yfiles.view.DisposeVisualCallback")[TYPE_PARAMETERS].getJSONObject(0).also {
        it[NAME] = "TVisual"
    }
}
