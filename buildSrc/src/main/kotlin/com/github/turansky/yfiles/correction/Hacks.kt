package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.removeItem
import com.github.turansky.yfiles.json.strictRemove
import org.json.JSONObject

internal fun applyHacks(api: JSONObject) {
    val source = Source(api)

    fieldToProperties(source)

    removeUnusedFunctionSignatures(source)
    removeDuplicatedProperties(source)
    removeDuplicatedMethods(source)
    removeInvalidOverrides(source)
    removeSystemMethods(source)
    removeArtificialParameters(source)
    removeThisParameters(source)
    removeTypeMetadataMethods(source)

    fixChangeHandlers(source)

    fixConstantGenerics(source)

    fixReturnType(source)

    fixConstantType(source)
    fixPropertyType(source)
    fixPropertyNullability(source)

    fixConstructorParameterName(source)

    fixMethodParameterName(source)
    fixMethodParameterType(source)
    fixMethodParameterNullability(source)
    fixMethodNullability(source)
    fixMethodGenericBounds(source)

    fixNullability(source)
    fixCollections(source)
    fixConvertibles(source)
    fixMarkupExtensions(source)
    fixConstructors(source)

    applyIdHacks(source)
    applyBindingHacks(source)
    applyBusinessObjectHacks(source)
    applyCloneableHacks(source)
    applyClassHacks(source)
    applyComparableHacks(source)
    applyContextMenuFixes(source)
    applyCursorHacks(source)
    applyItemDropInputModeHacks(source)

    applyListCellHacks(source)
    applyListHacks(source)
    applyYListHacks(source)
    applyEventHacks(source)
    applyExecutorHacks(source)

    applyLabelModelParameterHacks(source)
    applyMementoSupportHacks(source)
    applyClipboardHelperHacks(source)
    applyDragDropDataHacks(source)


    applyTagHacks(source)
    applyDataTagHacks(source)
    applyStyleTagHacks(source)
    applyLayoutDescriptorHacks(source)
    applyEventDispatcherHacks(source)
    applyCreationPropertiesHacks(source)

    applyContextLookupHacks(source)
    applyStyleRendererHacks(source)

    applyElementIdHacks(source)
    applySerializationHacks(source)
    applyCreationPropertyHacks(source)
    applyEdgeDirectednessHacks(source)

    applyExtensionHacks(source)
    applySingletonHacks(source)
    applyResultHacks(source)
    applyVisualHacks(source)

    addSizeExtensions(source)
}

private fun removeUnusedFunctionSignatures(source: Source) {
    source.functionSignatures.apply {
        UNUSED_FUNCTION_SIGNATURES.forEach {
            strictRemove(it)
        }
    }
}

private fun fixConstantGenerics(source: Source) {
    source.type("IListEnumerable")
        .constant("EMPTY")
        .replaceInType("<T>", "<*>")
}

private fun fixFunctionGenerics(source: Source) {
    source.type("IContextLookupChainLink")
        .method("addingLookupChainLink")
        .apply {
            setSingleTypeParameter("TResult")
            firstParameter.addGeneric("TResult")
        }
}

private fun fixReturnType(source: Source) {
    source.type("SvgExport")
        .method("exportSvg")[RETURNS][TYPE] = JS_SVG_SVG_ELEMENT
}

private fun fixConstantType(source: Source) {
    source.type("GraphCopier")
        .constant("NO_COPY")[TYPE] = JS_VOID
}

private fun fixPropertyType(source: Source) {
    source.type("IRenderContext")
        .property("defsElement")[TYPE] = JS_SVG_DEFS_ELEMENT
}

private fun fixPropertyNullability(source: Source) {
    PROPERTY_NULLABILITY_CORRECTION.forEach { (declaration, nullable) ->
        source.type(declaration.className)
            .property(declaration.propertyName)
            .changeNullability(nullable)
    }

    source.type("SvgVisualGroup")
        .property("children")
        .apply {
            require(get(TYPE) == "yfiles.collections.IList<yfiles.view.SvgVisual>")
            set(TYPE, "yfiles.collections.IList<yfiles.view.SvgVisual?>")
        }
}

private fun fixConstructorParameterName(source: Source) {
    for ((data, fixedName) in CONSTRUCTOR_PARAMETERS_CORRECTION) {
        source.type(data.className) {
            flatMap(CONSTRUCTORS)
                .flatMap(PARAMETERS)
                .filter { it[NAME] == data.parameterName }
                .ifEmpty { TODO("Check '${data.parameterName}'!") }
                .forEach { it[NAME] = fixedName }

            if (data.className != "TimeSpan") {
                check(
                    flatMap(PROPERTIES)
                        .any { it[NAME] == fixedName }
                )
            }
        }
    }
}

private fun fixMethodParameterName(source: Source) {
    PARAMETERS_CORRECTION.forEach { (data, fixedName) ->
        source.type(data.className)
            .methodParameters(data.methodName, data.parameterName, { it[NAME] != fixedName })
            .first()
            .set(NAME, fixedName)
    }
}

private fun fixMethodParameterNullability(source: Source) {
    PARAMETERS_NULLABILITY_CORRECTION
        .forEach { (data, nullable) ->
            val parameters = source.type(data.className)
                .methodParameters(data.methodName, data.parameterName)

            val parameter = if (data.last) {
                parameters.last()
            } else {
                parameters.first()
            }

            parameter.changeNullability(nullable)
        }

    source.types()
        .optFlatMap(METHODS)
        .filter { it[NAME] in BROKEN_NULLABILITY_METHODS }
        .filter { it[PARAMETERS].length() == 1 }
        .map { it[PARAMETERS].single() }
        .map { it as JSONObject }
        .onEach { require(it[TYPE] == LAYOUT_GRAPH) }
        .forEach { it.changeNullability(false) }

    source.types()
        .flatMap { it.allMethodParameters() }
        .filter { it[NAME] == "dataHolder" }
        .forEach { it.changeNullability(false) }

    source.types(
        "ModelManager",
        "FocusIndicatorManager",
        "HighlightIndicatorManager",
        "SelectionIndicatorManager"
    ).flatMap { it.flatMap(METHODS) }
        .filter { it[NAME] in MODEL_MANAGER_ITEM_METHODS }
        .map { it.firstParameter }
        .forEach { it.changeNullability(false) }
}

private fun fixMethodParameterType(source: Source) {
    source.type("IEnumerable")
        .method("concat")
        .parameter("elements")[TYPE] = "$IENUMERABLE<T>"

    source.type("SvgExport")
        .method("exportSvgString")
        .parameter("svg")[TYPE] = JS_SVG_ELEMENT

    source.type("CreateEdgeInputMode") {
        val PCC = "$ITEM_EVENT_ARGS<$IPORT_CANDIDATE>"

        flatMap(METHODS)
            .optFlatMap(PARAMETERS)
            .filter { it[TYPE] == PCC }
            .forEach { it.replaceInType(">", "?>") }
    }

    source.type("SvgVisual")
        .flatMap(METHODS)
        .filter { it[NAME] == "setScale" || it[NAME] == "setTranslate" }
        .map { it.firstParameter }
        .onEach { check(it[TYPE] == JS_ELEMENT) }
        .forEach { it[TYPE] = JS_SVG_ELEMENT }
}

private fun fixMethodNullability(source: Source) {
    METHOD_NULLABILITY_MAP.forEach { (declaration, nullable) ->
        source.type(declaration.className)
            .flatMap(METHODS)
            .filter { it[NAME] == declaration.methodName }
            .forEach { it.changeNullability(nullable) }
    }
}

private fun removeDuplicatedProperties(source: Source) {
    DUPLICATED_PROPERTIES
        .forEach { declaration ->
            val properties = source
                .type(declaration.className)[PROPERTIES]

            properties
                .map { it as JSONObject }
                .filter { it[NAME] == declaration.propertyName }
                .drop(1)
                .forEach { properties.removeItem(it) }
        }
}

private fun removeInvalidOverrides(source: Source) {
    INVALID_PROPERTY_OVERRIDES
        .forEach { declaration ->
            val properties = source
                .type(declaration.className)[PROPERTIES]

            properties
                .map { it as JSONObject }
                .filter { it[NAME] == declaration.propertyName }
                .forEach { properties.removeItem(it) }
        }
    INVALID_METHOD_OVERRIDES
        .forEach { declaration ->
            val methods = source
                .type(declaration.className)[METHODS]

            methods
                .map { it as JSONObject }
                .filter { it[NAME] == declaration.methodName }
                .forEach { methods.removeItem(it) }
        }
}

private fun removeDuplicatedMethods(source: Source) {
    DUPLICATED_METHODS
        .forEach { declaration ->
            val methods = source
                .type(declaration.className)[METHODS]

            methods
                .map { it as JSONObject }
                .filter { it[NAME] == declaration.methodName }
                .drop(1)
                .forEach { methods.removeItem(it) }
        }
}

private fun removeSystemMethods(source: Source) {
    source.types()
        .filter { it.has(METHODS) }
        .forEach {
            val methods = it[METHODS]
            val systemMetods = methods.asSequence()
                .map { it as JSONObject }
                .filter { it[NAME] in SYSTEM_FUNCTIONS }
                .toList()

            systemMetods.forEach {
                methods.removeItem(it)
            }
        }
}

private fun removeArtificialParameters(source: Source) {
    sequenceOf(CONSTRUCTORS, METHODS)
        .flatMap { parameter -> source.types().optFlatMap(parameter) }
        .filter { it.has(PARAMETERS) }
        .forEach {
            val artifitialParameters = it.flatMap(PARAMETERS)
                .filter { it.has(MODIFIERS) }
                .filter { it[MODIFIERS].contains(ARTIFICIAL) }
                .toList()

            val parameters = it[PARAMETERS]
            artifitialParameters.forEach {
                parameters.removeItem(it)
            }
        }
}

private val THIS_TYPES = setOf(
    "IEnumerable",
    "List"
)

private const val FUNC_RUDIMENT_1 = ",number,$IENUMERABLE<T>"
private const val FUNC_RUDIMENT_2 = ",$IENUMERABLE<T>,number"
private const val FROM_FUNC_RUDIMENT = "Func4<TSource,number,Object,T>"

private fun removeThisParameters(source: Source) {
    sequenceOf(CONSTRUCTORS, METHODS)
        .flatMap { parameter ->
            THIS_TYPES.asSequence()
                .map { source.type(it) }
                .optFlatMap(parameter)
        }
        .filter { it.has(PARAMETERS) }
        .map { it[PARAMETERS] }
        .filter { it.length() > 0 }
        .onEach {
            if ((it.last() as JSONObject)[NAME] == "thisArg") {
                it.strictRemove(it.length() - 1)
            }
        }
        .flatMap { it.asSequence() }
        .map { it as JSONObject }
        .filter { it.has(SIGNATURE) }
        .forEach {
            val signature = it[SIGNATURE]
            if (FUNC_RUDIMENT_1 in signature || FUNC_RUDIMENT_2 in signature) {
                it[SIGNATURE] = signature
                    .replace(FUNC_RUDIMENT_1, "")
                    .replace(FUNC_RUDIMENT_2, "")
                    .replace("Action3<T>", "Action1<T>")
                    .replace("Func4<T,boolean>", "Predicate<T>")
                    .replace("Func4<", "Func2<")
                    .replace("Func5<", "Func3<")
            } else if (FROM_FUNC_RUDIMENT in signature) {
                it[SIGNATURE] = signature.replace(FROM_FUNC_RUDIMENT, "Func2<TSource,T>")
            }
        }
}

private fun fieldToProperties(source: Source) {
    source.types()
        .filter { it.has(FIELDS) }
        .forEach { type ->
            if (type[GROUP] == "enum") {
                require(!type.has(CONSTANTS))
                type[CONSTANTS] = type[FIELDS]
                type.strictRemove(FIELDS)
                return@forEach
            }

            val additionalProperties = type.flatMap(FIELDS)
                .filter { STATIC !in it[MODIFIERS] }
                .onEach {
                    val modifiers = it[MODIFIERS]
                    modifiers.put(if (FINAL in modifiers) RO else FINAL)
                }
                .toList()

            if (type.has(PROPERTIES)) {
                val properties = type[PROPERTIES]
                additionalProperties.forEach { properties.put(it) }
            } else {
                type[PROPERTIES] = additionalProperties
            }

            val additionalConstants = type.flatMap(FIELDS)
                .filter { it !in additionalProperties }
                .toList()

            if (additionalConstants.isNotEmpty()) {
                require(!type.has(CONSTANTS))
                type[CONSTANTS] = additionalConstants
            }

            type.strictRemove(FIELDS)
        }
}

private fun JSONObject.addMethod(
    methodData: MethodData,
) {
    if (!has(METHODS)) {
        set(METHODS, emptyList<Any>())
    }

    val result = methodData.result
    var modifiers = listOf(PUBLIC)
    if (result != null) {
        modifiers += result.modifiers
    }

    get(METHODS)
        .put(
            mutableMapOf(
                NAME to methodData.methodName,
                MODIFIERS to modifiers
            )
                .also {
                    val parameters = methodData.parameters
                    if (parameters.isNotEmpty()) {
                        it.put(
                            PARAMETERS,
                            parameters.map {
                                mapOf(
                                    NAME to it.name,
                                    TYPE to it.type,
                                    MODIFIERS to it.modifiers
                                )
                            }
                        )
                    }
                }
                .also {
                    if (result != null) {
                        it.put(
                            RETURNS, mapOf(
                                TYPE to result.type
                            )
                        )
                    }
                }
        )
}

private fun fixMethodGenericBounds(source: Source) {
    val methodNames = setOf(
        "getMasterItem",
        "getViewItem"
    )
    source.type("IFoldingView")
        .flatMap(METHODS)
        .filter { it[NAME] in methodNames }
        .map { it[TYPE_PARAMETERS] }
        .map { it.single() as JSONObject }
        .forEach { it[BOUNDS] = arrayOf(IMODEL_ITEM) }

    source.type("HtmlVisual")
        .method("from")
        .get(TYPE_PARAMETERS)
        .getJSONObject(0)[BOUNDS] = arrayOf("Element")

    source.type("SvgVisual")
        .method("from")
        .get(TYPE_PARAMETERS)
        .getJSONObject(0)[BOUNDS] = arrayOf("SVGElement")
}
