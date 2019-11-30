package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.removeItem
import com.github.turansky.yfiles.json.strictRemove
import org.json.JSONObject

internal fun applyHacks(api: JSONObject) {
    val source = Source(api)

    cleanYObject(source)

    removeUnusedFunctionSignatures(source)
    removeDuplicatedProperties(source)
    removeDuplicatedMethods(source)
    removeSystemMethods(source)
    removeArtifitialParameters(source)
    removeThisParameters(source)

    fixUnionMethods(source)
    fixConstantGenerics(source)
    fixFunctionGenerics(source)

    fixReturnType(source)

    fixPropertyType(source)
    fixPropertyNullability(source)

    fixConstructorParameterName(source)

    fixMethodParameterName(source)
    fixMethodParameterType(source)
    fixMethodParameterNullability(source)
    fixMethodNullability(source)
    fixMethodGenericBounds(source)

    fixNullability(source)

    addMissedProperties(source)
    addMissedMethods(source)
    fieldToProperties(source)

    applyIdHacks(source)
    applyCloneableHacks(source)
    applyClassHacks(source)
    applyCollectionHacks(source)
    applyComparableHacks(source)
    applyComparerHacks(source)
    applyCursorHacks(source)
    applyDpataHacks(source)
    applyDataHacks(source)
    applyDpKeyHacks(source)
    applyListHacks(source)
    applyYListHacks(source)
    applyEventHacks(source)

    fixConstructors(source)
}

private fun cleanYObject(source: Source) {
    source.type("YObject").apply {
        strictRemove(STATIC_METHODS)
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

private fun fixUnionMethods(source: Source) {
    val methods = source.type("GraphModelManager")
        .get(METHODS)

    val unionMethods = methods
        .asSequence()
        .map { it as JSONObject }
        .filter { it[NAME] == "getCanvasObjectGroup" }
        .toList()

    unionMethods
        .asSequence()
        .drop(1)
        .forEach { methods.removeItem(it) }

    unionMethods.first()
        .firstParameter
        .apply {
            set(NAME, "item")
            set(TYPE, IMODEL_ITEM)
        }

    // TODO: remove documentation
}

private fun fixConstantGenerics(source: Source) {
    source.type("IListEnumerable")
        .get(CONSTANTS)
        .firstWithName("EMPTY")
        .also {
            it[TYPE] = it[TYPE]
                .replace("<T>", "<$JS_OBJECT>")
        }

    source.type("LabelLayoutKeys")
        .jsequence(CONSTANTS)
        .forEach {
            val type = it[TYPE]
            if (type.endsWith("<yfiles.layout.LabelLayoutData>")) {
                it[TYPE] = type.replace("<yfiles.layout.LabelLayoutData>", "<Array<yfiles.layout.LabelLayoutData>>")
            }
        }
}

private fun fixFunctionGenerics(source: Source) {
    source.type("List")
        .staticMethod("fromArray")
        .setSingleTypeParameter()

    source.type("List")
        .staticMethod("from")
        .get(TYPE_PARAMETERS)
        .put(jObject(NAME to "T"))

    source.type("IContextLookupChainLink")
        .staticMethod("addingLookupChainLink")
        .apply {
            setSingleTypeParameter("TResult")
            firstParameter.addGeneric("TResult")
        }
}

private fun fixReturnType(source: Source) {
    source.type("SvgExport").apply {
        get(METHODS)
            .firstWithName("exportSvg")
            .get(RETURNS)
            .set(TYPE, JS_SVG_SVG_ELEMENT)
    }
}

private fun fixPropertyType(source: Source) {
    source.types("SeriesParallelLayoutData", "TreeLayoutData")
        .forEach {
            it.property("outEdgeComparers")
                .set(TYPE, "yfiles.layout.ItemMapping<yfiles.graph.INode,Comparator<yfiles.graph.IEdge>>")
        }
}

private fun fixPropertyNullability(source: Source) {
    PROPERTY_NULLABILITY_CORRECTION.forEach { (className, propertyName), nullable ->
        source
            .type(className)
            .property(propertyName)
            .changeNullability(nullable)
    }

    source.type("SvgVisualGroup")
        .get(PROPERTIES)
        .firstWithName("children")
        .apply {
            require(get(TYPE) == "yfiles.collections.IList<yfiles.view.SvgVisual>")
            set(TYPE, "yfiles.collections.IList<yfiles.view.SvgVisual?>")
        }
}

private fun fixConstructorParameterName(source: Source) {
    source.type("TimeSpan")
        .jsequence(CONSTRUCTORS)
        .jsequence(PARAMETERS)
        .single { it[NAME] == "millis" }
        .set(NAME, "milliseconds")
}

private fun fixMethodParameterName(source: Source) {
    PARAMETERS_CORRECTION.forEach { data, fixedName ->
        source.type(data.className)
            .methodParameters(data.methodName, data.parameterName, { it[NAME] != fixedName })
            .first()
            .set(NAME, fixedName)
    }

    source.type("RankAssignmentAlgorithm")
        .jsequence(STATIC_METHODS)
        .filter { it[NAME] == "simplex" }
        .jsequence(PARAMETERS)
        .single { it[NAME] == "_root" }
        .set(NAME, "root")
}

private fun fixMethodParameterNullability(source: Source) {
    PARAMETERS_NULLABILITY_CORRECTION
        .forEach { data, nullable ->
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
        .optionalArray(METHODS)
        .filter { it[NAME] in BROKEN_NULLABILITY_METHODS }
        .filter { it[PARAMETERS].length() == 1 }
        .map { it[PARAMETERS].single() }
        .map { it as JSONObject }
        .onEach { require(it[TYPE] == "yfiles.layout.LayoutGraph") }
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
    ).flatMap { it.jsequence(METHODS) }
        .filter { it[NAME] in MODEL_MANAGER_ITEM_METHODS }
        .map { it.firstParameter }
        .forEach { it.changeNullability(false) }
}

private fun fixMethodParameterType(source: Source) {
    source.type("IContextLookupChainLink")
        .staticMethod("addingLookupChainLink")
        .parameter("instance")
        .set(TYPE, "TResult")

    source.type("SvgExport")
        .staticMethod("exportSvgString")
        .parameter("svg")
        .set(TYPE, JS_SVG_ELEMENT)
}

private fun fixMethodNullability(source: Source) {
    STATIC_METHOD_NULLABILITY_MAP
        .forEach { (className, methodName), nullable ->
            source.type(className)
                .jsequence(STATIC_METHODS)
                .filter { it[NAME] == methodName }
                .forEach { it.changeNullability(nullable) }
        }

    METHOD_NULLABILITY_MAP
        .forEach { (className, methodName), nullable ->
            source.type(className)
                .jsequence(METHODS)
                .filter { it[NAME] == methodName }
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

            val property = properties
                .firstWithName(declaration.propertyName)

            properties.removeItem(property)
        }
}

private fun removeDuplicatedMethods(source: Source) {
    DUPLICATED_METHODS
        .forEach { declaration ->
            val methods = source
                .type(declaration.className)
                .get(METHODS)

            val method = methods
                .firstWithName(declaration.methodName)

            methods.removeItem(method)
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
        .flatMap { parameter ->
            source.types()
                .filter { it.has(parameter) }
                .jsequence(parameter)
        }
        .filter { it.has(PARAMETERS) }
        .forEach {
            val artifitialParameters = it.jsequence(PARAMETERS)
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

private val FUNC_RUDIMENT = ",number,yfiles.collections.IEnumerable<T>"
private val FROM_FUNC_RUDIMENT = "Func4<TSource,number,Object,T>"

private fun removeThisParameters(source: Source) {
    sequenceOf(CONSTRUCTORS, STATIC_METHODS, METHODS)
        .flatMap { parameter ->
            THIS_TYPES.asSequence()
                .map { source.type(it) }
                .filter { it.has(parameter) }
                .jsequence(parameter)
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
            val fields = type[FIELDS].apply {
                asSequence()
                    .map { it as JSONObject }
                    .map { it[MODIFIERS] }
                    .forEach { it.put(if (FINAL in it) RO else FINAL) }
            }

            if (type.has(PROPERTIES)) {
                val properties = type[PROPERTIES]
                fields.forEach { properties.put(it) }
            } else {
                type[PROPERTIES] = fields
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
        .jsequence(METHODS)
        .filter { it[NAME] in methodNames }
        .map { it[TYPE_PARAMETERS] }
        .map { it.single() as JSONObject }
        .forEach { it[BOUNDS] = arrayOf(IMODEL_ITEM) }
}
