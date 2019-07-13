package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.CANBENULL
import com.github.turansky.yfiles.YCLASS
import org.json.JSONObject

internal fun applyClassHacks(source: Source) {
    addDpKeyGeneric(source)
    addClassGeneric(source)
}

private val DP_KEY_BASE = "DpKeyBase"
private val DP_KEY_BASE_KEY = "TKey"

private val DP_KEY_BASE_DECLARATION = "yfiles.algorithms.DpKeyBase<"

private val DP_KEY_GENERIC_MAP = mapOf(
    DP_KEY_BASE to "TKey",
    "EdgeDpKey" to "yfiles.graph.IEdge",
    "GraphDpKey" to "yfiles.graph.IGraph",
    "GraphObjectDpKey" to "yfiles.algorithms.GraphObject", // TODO: check generic
    "IEdgeLabelLayoutDpKey" to "yfiles.layout.IEdgeLabelLayout",
    "ILabelLayoutDpKey" to "yfiles.layout.ILabelLayout",
    "INodeLabelLayoutDpKey" to "yfiles.layout.INodeLabelLayout",
    "NodeDpKey" to "yfiles.graph.INode"
)

private fun addDpKeyGeneric(source: Source) {
    source.type(DP_KEY_BASE).apply {
        addFirstTypeParameter(DP_KEY_BASE_KEY)
        methodParameters(
            "equalsCore",
            "other",
            { true }
        ).single()
            .updateDpKeyGeneric(J_TYPE, DP_KEY_BASE_KEY)

        property("declaringType")
            .addGeneric(DP_KEY_BASE_KEY)
    }

    for ((className, generic) in DP_KEY_GENERIC_MAP) {
        val type = source.type(className)

        type.jsequence(J_CONSTRUCTORS)
            .map { it.parameter("declaringType") }
            .forEach { it.addGeneric(generic) }

        if (className == DP_KEY_BASE) {
            continue
        }

        type.updateDpKeyGeneric(J_EXTENDS, generic)
    }

    source.type("DpKeyItemCollection")
        .property("dpKey")
        .updateDpKeyGeneric(J_TYPE, "T")
}

private fun JSONObject.updateDpKeyGeneric(
    field: String,
    generic: String
) {
    val value = getString(field)
    require(value.startsWith(DP_KEY_BASE_DECLARATION))
    put(field, value.replace(DP_KEY_BASE_DECLARATION, "$DP_KEY_BASE_DECLARATION$generic,"))
}

private fun addClassGeneric(source: Source) {
    source.type("Class")
        .setSingleTypeParameter()

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