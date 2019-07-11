package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.first
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jObject
import org.json.JSONObject
import kotlin.collections.first

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

    fixConstructorParameterNullability(source)

    fixPropertyType(source)
    fixPropertyNullability(source)

    fixMethodParameterName(source)
    fixMethodParameterType(source)
    fixMethodParameterNullability(source)
    fixMethodNullability(source)

    addMissedProperties(source)
    addMissedMethods(source)
    fieldToProperties(source)

    addClassGeneric(source)
}

private fun removeUnusedFunctionSignatures(source: Source) {
    source.functionSignatures.apply {
        UNUSED_FUNCTION_SIGNATURES.forEach {
            requireNotNull(remove(it))
        }
    }
}

private fun addClassGeneric(source: Source) {
    source.type("Class")
        .addStandardGeneric()

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
            it.addStandardGeneric()

            it.typeParameter.addGeneric("T")

            // TODO: fix return type after ticket resolving
            //  https://youtrack.jetbrains.com/issue/KT-3257

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
        .apply {
            (jsequence(J_METHODS) + jsequence(J_STATIC_METHODS))
                .optionalArray(J_PARAMETERS)
                .filter { it.getString(J_TYPE) == YCLASS }
                .forEach {
                    when (it.getString(J_NAME)) {
                        "keyType" -> it.addGeneric("TKey")
                        "modelItemType" -> it.addGeneric("TKey")
                        "dataType" -> it.addGeneric("TData")
                    }
                }
        }


    source.allMethods(
        "addMapper",
        "addConstantMapper",
        "addDelegateMapper",

        // "createMapper",
        "createConstantMapper",
        "createDelegateMapper",

        "addDataProvider",
        "createDataMap",
        "createDataProvider"
    )
        .filter { it.firstParameter.getString(J_NAME) == "keyType" }
        .forEach {
            it.parameter("keyType").addGeneric("K")
            it.parameter("valueType").addGeneric("V")
        }

    source.types()
        .forEach { type ->
            val typeName = type.getString(J_NAME)
            if (typeName == "MapperMetadata") {
                return@forEach
            }

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
        .forEach { methods.remove(methods.indexOf(it)) }

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
        .getJSONArray(J_STATIC_METHODS)
        .firstWithName("fromArray")
        .addStandardGeneric()

    source.type("List")
        .getJSONArray(J_STATIC_METHODS)
        .firstWithName("from")
        .getJSONArray(J_TYPE_PARAMETERS)
        .put(jObject(J_NAME to "T"))

    source.type("IContextLookupChainLink")
        .getJSONArray(J_STATIC_METHODS)
        .firstWithName("addingLookupChainLink")
        .apply {
            addStandardGeneric("TResult")
            firstParameter.addGeneric("TResult")
        }
}

private fun fixReturnType(source: Source) {
    sequenceOf("EdgeList", "YNodeList")
        .map { source.type(it) }
        .forEach {
            it.getJSONArray(J_METHODS)
                .firstWithName("getEnumerator")
                .getJSONObject(J_RETURNS)
                .put(J_TYPE, "yfiles.collections.IEnumerator<$JS_OBJECT>")
        }
}

private fun fixImplementedTypes(source: Source) {
    sequenceOf("EdgeList", "YNodeList")
        .map { source.type(it) }
        .forEach { it.remove("implements") }
}

private fun fixConstructorParameterNullability(source: Source) {
    STRICT_CONSTRUCTOR_CLASSES
        .asSequence()
        .map { source.type(it) }
        .forEach {
            it.jsequence(J_CONSTRUCTORS)
                .jsequence(J_PARAMETERS)
                .forEach { it.changeNullability(false, false) }
        }
}

private fun fixPropertyType(source: Source) {
    sequenceOf("SeriesParallelLayoutData", "TreeLayoutData")
        .map { source.type(it) }
        .forEach {
            it.getJSONArray(J_PROPERTIES)
                .firstWithName("outEdgeComparers")
                .put(J_TYPE, "yfiles.layout.ItemMapping<yfiles.graph.INode,Comparator<yfiles.graph.IEdge>>")
        }
}

private fun fixPropertyNullability(source: Source) {
    PROPERTY_NULLABILITY_CORRECTION.forEach { (className, propertyName), nullable ->
        source
            .type(className)
            .getJSONArray(J_PROPERTIES)
            .first { it.get(J_NAME) == propertyName }
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
}

private fun fixMethodParameterType(source: Source) {
    source.type("IContextLookupChainLink")
        .getJSONArray(J_STATIC_METHODS)
        .firstWithName("addingLookupChainLink")
        .parameter("instance")
        .put(J_TYPE, "TResult")
}

private fun fixMethodNullability(source: Source) {
    METHOD_NULLABILITY_MAP
        .forEach { (className, methodName), nullable ->
            source.type(className)
                .getJSONArray(J_METHODS)
                .firstWithName(methodName)
                .changeNullability(nullable)
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
                .getJSONArray(J_PROPERTIES)

            val property = properties
                .firstWithName(declaration.propertyName)

            properties.remove(properties.indexOf(property))
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

            methods.remove(methods.indexOf(method))
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
                methods.remove(methods.indexOf(it))
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
                parameters.remove(parameters.indexOf(it))
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
                it.remove(it.length() - 1)
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
            type.remove(J_FIELDS)
        }
}