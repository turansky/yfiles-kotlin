package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import com.github.turansky.yfiles.json.removeItem
import com.github.turansky.yfiles.json.strictRemove
import org.json.JSONObject

internal fun applyHacks(api: JSONObject) {
    val source = Source(api)

    fieldToProperties(source)

    cleanYObject(source)

    removeUnusedFunctionSignatures(source)
    removeDuplicatedProperties(source)
    removeDuplicatedMethods(source)
    removeSystemMethods(source)
    removeArtifitialParameters(source)
    removeThisParameters(source)

    fixConstantGenerics(source)
    fixFunctionGenerics(source)

    fixReturnType(source)

    fixConstantType(source)
    fixPropertyType(source)
    fixPropertyNullability(source)

    fixConstructorParameterName(source)

    fixMethodParameterName(source)
    fixMethodParameterType(source)
    fixMethodParameterOptionality(source)
    fixMethodParameterNullability(source)
    fixMethodNullability(source)
    fixMethodGenericBounds(source)

    fixNullability(source)

    addMissedProperties(source)
    addMissedMethods(source)

    applyIdHacks(source)
    applyBindingHacks(source)
    applyBusinessObjectHacks(source)
    applyCloneableHacks(source)
    applyClassHacks(source)
    applyCollectionHacks(source)
    applyComparableHacks(source)
    applyComparerHacks(source)
    applyCursorHacks(source)
    applyPartitionCellHacks(source)
    applyIntersectionHacks(source)

    applyDpdataHacks(source)
    applyDataHacks(source)
    applyDpKeyHacks(source)

    applyListCellHacks(source)
    applyListHacks(source)
    applyYListHacks(source)
    applyEventHacks(source)

    applyLabelModelParameterHacks(source)
    applyMementoHacks(source)
    applyMementoSupportHacks(source)
    applyClipboardHelperHacks(source)
    applySnapLineProviderHacks(source)
    applyIncrementalHintHacks(source)
    applyObstacleDataHacks(source)
    applyTooltipHacks(source)
    applyDragDropDataHacks(source)

    applyTagHacks(source)
    applyDataTagHacks(source)
    applyStyleTagHacks(source)
    applyResourceHacks(source)
    applyTemplatesHacks(source)
    applyTemplateLoadersHacks(source)
    applyConvertersHacks(source)
    applyEventDispatcherHacks(source)
    applyCommandHacks(source)

    applyContextLookupHacks(source)
    applyCanvasObjectDescriptorHacks(source)
    applyCanvasObjectInstallerHacks(source)
    applyVisualTemplateHacks(source)
    applyStyleRendererHacks(source)

    applyElementIdHacks(source)
    applySerializationHacks(source)
    applyCreationPropertyHacks(source)
    applyEdgeDirectednessHacks(source)

    applyExtensionHacks(source)
    applySingletonHacks(source)
    applyResultHacks(source)
    fixConstructors(source)

    applyLayoutStrictTypes(source)
    addSizeExtensions(source)
}

private fun cleanYObject(source: Source) {
    source.type("YObject") {
        set(GROUP, "interface")

        strictRemove(METHODS)
    }
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
    source.type("DiscreteEdgeLabelLayoutModel")
        .method("getPosition")[RETURNS][TYPE] = "yfiles.layout.DiscreteEdgeLabelPositions"

    source.type("SvgExport")
        .method("exportSvg")[RETURNS][TYPE] = JS_SVG_SVG_ELEMENT

    source.type("PortCandidateSet")
        .property("entries")
        .also { it.replaceInType("<$JS_ANY>", "<IPortCandidateSetEntry>") }
}

private fun fixConstantType(source: Source) {
    source.type("GraphCopier")
        .constant("NO_COPY")[TYPE] = JS_VOID
}

private fun fixPropertyType(source: Source) {
    source.type("IRenderContext")
        .property("defsElement")
        .set(TYPE, JS_SVG_DEFS_ELEMENT)

    source.types("SeriesParallelLayoutData", "TreeLayoutData")
        .forEach {
            it.property("outEdgeComparers")
                .set(TYPE, "yfiles.layout.ItemMapping<$INODE,yfiles.collections.IComparer<$IEDGE>>")
        }
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
    source.type("TimeSpan")
        .flatMap(CONSTRUCTORS)
        .flatMap(PARAMETERS)
        .single { it[NAME] == "millis" }
        .set(NAME, "milliseconds")

    source.type("SvgDefsManager")
        .flatMap(CONSTRUCTORS)
        .flatMap(PARAMETERS)
        .single { it[NAME] == "defsElement" }
        .set(NAME, "defs")

    source.type("LayoutGraphHider")
        .flatMap(CONSTRUCTORS)
        .flatMap(PARAMETERS)
        .single { it[NAME] == "g" }
        .set(NAME, "graph")

    val nameMap = mapOf(
        "p1" to "firstEndPoint",
        "p2" to "secondEndPoint"
    )
    source.type("LineSegment")
        .flatMap(CONSTRUCTORS)
        .flatMap(PARAMETERS)
        .forEach { it[NAME] = nameMap.getValue(it[NAME]) }
}

private fun fixMethodParameterName(source: Source) {
    PARAMETERS_CORRECTION.forEach { (data, fixedName) ->
        source.type(data.className)
            .methodParameters(data.methodName, data.parameterName, { it[NAME] != fixedName })
            .first()
            .set(NAME, fixedName)
    }
}

private fun fixMethodParameterOptionality(source: Source) {
    val methodNames = setOf("onShow", "show")

    source.type("MouseHoverInputMode")
        .flatMap(METHODS)
        .filter { it[NAME] in methodNames }
        .flatMap(PARAMETERS)
        .filter { it[NAME] == "content" }
        .map { it[MODIFIERS] }
        .forEach { it.removeItem(OPTIONAL) }
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

    source.types()
        .optFlatMap(EVENTS)
        .eventListeners()
        .flatMap(PARAMETERS)
        .forEach { it.changeNullability(false) }
}

private fun fixMethodParameterType(source: Source) {
    source.type("IEnumerable")
        .method("concat")
        .parameter("elements")
        .set(TYPE, "$IENUMERABLE<T>")

    source.type("IContextLookupChainLink")
        .method("addingLookupChainLink")
        .parameter("instance")
        .set(TYPE, "TResult")

    source.type("DiscreteEdgeLabelLayoutModel")
        .method("createPositionParameter")
        .parameter("position")[TYPE] = "yfiles.layout.DiscreteEdgeLabelPositions"

    source.type("SvgExport")
        .method("exportSvgString")
        .parameter("svg")
        .set(TYPE, JS_SVG_ELEMENT)

    source.type("AspectRatioTreeLayout")
        .flatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "localRoot" }
        .filter { it[TYPE] == JS_OBJECT }
        .forEach { it[TYPE] = NODE }

    source.type("CreateEdgeInputMode") {
        val PCC = "$ITEM_EVENT_ARGS<$IPORT_CANDIDATE>"

        flatMap(METHODS)
            .optFlatMap(PARAMETERS)
            .filter { it[TYPE] == PCC }
            .forEach { it.replaceInType(">", "?>") }

        val PCC_HANDLER = "$EVENT_HANDLER1<$PCC>"
        flatMap(EVENTS)
            .eventListeners()
            .flatMap(PARAMETERS)
            .filter { it[SIGNATURE] == PCC_HANDLER }
            .forEach { it.replaceInSignature(">>", "?>>") }
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

private fun addMissedProperties(source: Source) {
    MISSED_PROPERTIES
        .forEach { data ->
            source.type(data.className)
                .addProperty(data.propertyName, data.type)
        }
}

private fun addMissedMethods(source: Source) {
    MISSED_METHODS.forEach { data ->
        source.type(data.className)
            .addMethod(data)
    }
}

private fun removeDuplicatedProperties(source: Source) {
    DUPLICATED_PROPERTIES
        .forEach { declaration ->
            val properties = source
                .type(declaration.className)
                .get(PROPERTIES)

            properties.removeItem(properties[declaration.propertyName])
        }
}

private fun removeDuplicatedMethods(source: Source) {
    DUPLICATED_METHODS
        .forEach { declaration ->
            val methods = source
                .type(declaration.className)
                .get(METHODS)

            methods.removeItem(methods[declaration.methodName])
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

private fun removeArtifitialParameters(source: Source) {
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

private const val FUNC_RUDIMENT = ",number,$IENUMERABLE<T>"
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
            if (FUNC_RUDIMENT in signature) {
                it[SIGNATURE] = signature
                    .replace(FUNC_RUDIMENT, "")
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
    methodData: MethodData
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
}
