package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ARTIFICIAL
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.PUBLIC
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.removeItem
import com.github.turansky.yfiles.json.strictRemove
import org.json.JSONObject

internal fun applyHacks(api: JSONObject) {
    val source = Source(api)

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
    fixImplementedTypes(source)

    fixPropertyType(source)
    fixPropertyNullability(source)

    fixMethodParameterName(source)
    fixMethodParameterType(source)
    fixMethodParameterNullability(source)
    fixMethodNullability(source)
    fixAlgorithmsNullability(source)

    addMissedProperties(source)
    addMissedMethods(source)
    fieldToProperties(source)

    applyClassHacks(source)
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
        .getJSONArray(J_METHODS)

    val unionMethods = methods
        .asSequence()
        .map { it as JSONObject }
        .filter { it.getString(J_NAME) == "getCanvasObjectGroup" }
        .toList()

    unionMethods
        .asSequence()
        .drop(1)
        .forEach { methods.removeItem(it) }

    unionMethods.first()
        .firstParameter
        .apply {
            put(J_NAME, "item")
            put(J_TYPE, "yfiles.graph.IModelItem")
        }

    // TODO: remove documentation
}

private fun fixConstantGenerics(source: Source) {
    source.type("IListEnumerable")
        .getJSONArray("constants")
        .firstWithName("EMPTY")
        .also {
            val type = it.getString(J_TYPE)
                .replace("<T>", "<$JS_OBJECT>")
            it.put(J_TYPE, type)
        }
}

private fun fixFunctionGenerics(source: Source) {
    source.type("List")
        .staticMethod("fromArray")
        .setSingleTypeParameter()

    source.type("List")
        .staticMethod("from")
        .getJSONArray(J_TYPE_PARAMETERS)
        .put(jObject(J_NAME to "T"))

    source.type("IContextLookupChainLink")
        .staticMethod("addingLookupChainLink")
        .apply {
            setSingleTypeParameter("TResult")
            firstParameter.addGeneric("TResult")
        }
}

private fun fixReturnType(source: Source) {
    source.types("EdgeList", "YNodeList")
        .forEach {
            it.getJSONArray(J_METHODS)
                .firstWithName("getEnumerator")
                .getJSONObject(J_RETURNS)
                .put(J_TYPE, "yfiles.collections.IEnumerator<$JS_OBJECT>")
        }
}

private fun fixImplementedTypes(source: Source) {
    source.types("EdgeList", "YNodeList")
        .forEach { it.strictRemove("implements") }
}

private fun fixPropertyType(source: Source) {
    source.types("SeriesParallelLayoutData", "TreeLayoutData")
        .forEach {
            it.property("outEdgeComparers")
                .put(J_TYPE, "yfiles.layout.ItemMapping<yfiles.graph.INode,Comparator<yfiles.graph.IEdge>>")
        }
}

private fun fixPropertyNullability(source: Source) {
    PROPERTY_NULLABILITY_CORRECTION.forEach { (className, propertyName), nullable ->
        source
            .type(className)
            .property(propertyName)
            .changeNullability(nullable)
    }
}

private fun fixMethodParameterName(source: Source) {
    PARAMETERS_CORRECTION.forEach { data, fixedName ->
        source.type(data.className)
            .methodParameters(data.methodName, data.parameterName, { it.getString(J_NAME) != fixedName })
            .first()
            .put(J_NAME, fixedName)
    }
}

private fun fixMethodParameterNullability(source: Source) {
    PARAMETERS_NULLABILITY_CORRECTION
        .forEach { data, nullable ->
            val parameters = source.type(data.className)
                .methodParameters(data.methodName, data.parameterName, { true })

            val parameter = if (data.last) {
                parameters.last()
            } else {
                parameters.first()
            }

            parameter.changeNullability(nullable)
        }

    source.types()
        .optionalArray(J_METHODS)
        .filter { it.get(J_NAME) in BROKEN_NULLABILITY_METHODS }
        .filter { it.getJSONArray(J_PARAMETERS).length() == 1 }
        .map { it.getJSONArray(J_PARAMETERS).single() }
        .map { it as JSONObject }
        .onEach { require(it.getString(J_TYPE) == "yfiles.layout.LayoutGraph") }
        .forEach { it.changeNullability(false) }

    source.types()
        .flatMap { it.allMethodParameters() }
        .filter { it.get(J_NAME) == "dataHolder" }
        .forEach { it.changeNullability(false) }

    source.types(
        "ModelManager",
        "FocusIndicatorManager",
        "HighlightIndicatorManager",
        "SelectionIndicatorManager"
    ).flatMap { it.jsequence(J_METHODS) }
        .filter { it.getString(J_NAME) in MODEL_MANAGER_ITEM_METHODS }
        .map { it.firstParameter }
        .forEach { it.changeNullability(false) }
}

private fun fixMethodParameterType(source: Source) {
    source.type("IContextLookupChainLink")
        .staticMethod("addingLookupChainLink")
        .parameter("instance")
        .put(J_TYPE, "TResult")
}

private fun fixMethodNullability(source: Source) {
    METHOD_NULLABILITY_MAP
        .forEach { (className, methodName), nullable ->
            source.type(className)
                .jsequence(J_METHODS)
                .filter { it.getString(J_NAME) == methodName }
                .forEach { it.changeNullability(nullable) }
        }
}

private fun fixAlgorithmsNullability(source: Source) {
    val EXCLUDED_PARAMETERS = setOf(
        "edgeCosts",
        "edgeWeights",

        "defaultValue",
        "dualsNM",

        "revMap",
        "reverseEdgeMap"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number",

        "yfiles.algorithms.Linkage",
        "yfiles.algorithms.DistanceMetric"
    )

    source.types(
        "AbortHandler",
        "AffineLine",
        "BipartitionAlgorithm",
        "CentralityAlgorithm",
        "Comparers",
        "Cursors",
        "CycleAlgorithm",
        "DataProviders",
        "GraphChecker",
        "GraphConnectivity",
        "GroupAlgorithm",
        "IndependentSetAlgorithm",
        "IntersectionAlgorithm",
        "Maps",
        "NodeOrderAlgorithm",
        "PathAlgorithm",
        "SortingAlgorithm",
        "SpanningTreeAlgorithm",
        "TransitivityAlgorithm",
        "TreeAlgorithm",
        "TriangulationAlgorithm"
    ).jsequence(J_STATIC_METHODS)
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_NAME) in EXCLUDED_PARAMETERS }
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
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
                .getJSONArray(J_PROPERTIES)

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
                .getJSONArray(J_METHODS)

            val method = methods
                .firstWithName(declaration.methodName)

            methods.removeItem(method)
        }
}

private fun removeSystemMethods(source: Source) {
    source.types()
        .filter { it.has(J_METHODS) }
        .forEach {
            val methods = it.getJSONArray(J_METHODS)
            val systemMetods = methods.asSequence()
                .map { it as JSONObject }
                .filter { it.getString(J_NAME) in SYSTEM_FUNCTIONS }
                .toList()

            systemMetods.forEach {
                methods.removeItem(it)
            }
        }
}

private fun removeArtifitialParameters(source: Source) {
    sequenceOf(J_CONSTRUCTORS, J_METHODS)
        .flatMap { parameter ->
            source.types()
                .filter { it.has(parameter) }
                .jsequence(parameter)
        }
        .filter { it.has(J_PARAMETERS) }
        .forEach {
            val artifitialParameters = it.jsequence(J_PARAMETERS)
                .filter { it.getJSONArray(J_MODIFIERS).contains(ARTIFICIAL) }
                .toList()

            val parameters = it.getJSONArray(J_PARAMETERS)
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
    sequenceOf(J_CONSTRUCTORS, J_STATIC_METHODS, J_METHODS)
        .flatMap { parameter ->
            THIS_TYPES.asSequence()
                .map { source.type(it) }
                .filter { it.has(parameter) }
                .jsequence(parameter)
        }
        .filter { it.has(J_PARAMETERS) }
        .map { it.getJSONArray(J_PARAMETERS) }
        .filter { it.length() > 0 }
        .onEach {
            if ((it.last() as JSONObject).getString(J_NAME) == "thisArg") {
                it.strictRemove(it.length() - 1)
            }
        }
        .flatMap { it.asSequence() }
        .map { it as JSONObject }
        .filter { it.has(J_SIGNATURE) }
        .forEach {
            var signature = it.getString(J_SIGNATURE)
            if (signature.contains(FUNC_RUDIMENT)) {
                signature = signature
                    .replace(FUNC_RUDIMENT, "")
                    .replace("Action3<T>", "Action1<T>")
                    .replace("Func4<T,boolean>", "Predicate<T>")
                    .replace("Func4<", "Func2<")
                    .replace("Func5<", "Func3<")

                it.put(J_SIGNATURE, signature)
            } else if (signature.contains(FROM_FUNC_RUDIMENT)) {
                it.put(J_SIGNATURE, signature.replace(FROM_FUNC_RUDIMENT, "Func2<TSource,T>"))
            }
        }
}

private fun fieldToProperties(source: Source) {
    source.types()
        .filter { it.has(J_FIELDS) }
        .forEach { type ->
            val fields = type.getJSONArray(J_FIELDS)
            if (type.has(J_PROPERTIES)) {
                val properties = type.getJSONArray(J_PROPERTIES)
                fields.forEach { properties.put(it) }
            } else {
                type.put(J_PROPERTIES, fields)
            }
            type.strictRemove(J_FIELDS)
        }
}

private fun JSONObject.addMethod(
    methodData: MethodData
) {
    if (!has(J_METHODS)) {
        put(J_METHODS, emptyList<Any>())
    }

    val result = methodData.result
    var modifiers = listOf(PUBLIC)
    if (result != null) {
        modifiers += result.modifiers
    }

    getJSONArray(J_METHODS)
        .put(
            mutableMapOf(
                J_NAME to methodData.methodName,
                J_MODIFIERS to modifiers
            )
                .also {
                    val parameters = methodData.parameters
                    if (parameters.isNotEmpty()) {
                        it.put(
                            J_PARAMETERS,
                            parameters.map {
                                mapOf(
                                    J_NAME to it.name,
                                    J_TYPE to it.type,
                                    J_MODIFIERS to it.modifiers
                                )
                            }
                        )
                    }
                }
                .also {
                    if (result != null) {
                        it.put(
                            J_RETURNS, mapOf(
                                J_TYPE to result.type
                            )
                        )
                    }
                }
        )
}
