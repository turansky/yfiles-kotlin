package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

internal fun applyDpataHacks(source: Source) {
    fixGraph(source)
    fixLayoutGraphAdapter(source)
    fixTreeLayout(source)
    fixGraphPartitionManager(source)
    fixHierarchicLayoutCore(source)

    fixDataProviders(source)
}

private val GENERIC_DP_KEY = "yfiles.algorithms.DpKeyBase<K,V>"

private fun fixGraph(source: Source) {
    val methods = source.type("Graph")[METHODS]

    methods["getDataProvider"].apply {
        setKeyValueTypeParameters()
        firstParameter[TYPE] = GENERIC_DP_KEY

        get(RETURNS)
            .addGeneric("K,V")
    }

    methods["addDataProvider"].apply {
        setKeyValueTypeParameters()
        firstParameter[TYPE] = GENERIC_DP_KEY

        secondParameter.addGeneric("K,V")
    }

    sequenceOf("createEdgeMap", "createNodeMap")
        .map { methods[it] }
        .forEach {
            it.setSingleTypeParameter("V", JS_OBJECT)

            it[RETURNS]
                .addGeneric("V")
        }
}

private fun fixLayoutGraphAdapter(source: Source) {
    val methods = source.type("LayoutGraphAdapter")[METHODS]

    methods["getDataProvider"].apply {
        setKeyValueTypeParameters()
        firstParameter[TYPE] = GENERIC_DP_KEY

        get(RETURNS)
            .addGeneric("K,V")
    }

    methods["addDataProvider"].apply {
        get(PARAMETERS)
            .get("dataKey")
            .set(TYPE, GENERIC_DP_KEY)

        get(RETURNS)
            .addGeneric("K,V")
    }
}

private fun fixTreeLayout(source: Source) {
    val properties = source.type("TreeLayout")[PROPERTIES]

    sequenceOf(
        "sourceGroupDataAcceptor" to YID,
        "sourcePortConstraintDataAcceptor" to "yfiles.layout.PortConstraint",
        "targetGroupDataAcceptor" to YID,
        "targetPortConstraintDataAcceptor" to "yfiles.layout.PortConstraint"
    ).forEach { (propertyName, valueType) ->
        properties[propertyName]
            .addGeneric("$EDGE,$valueType")
    }

    source.type("TreeComponentLayout")
        .get(METHODS)
        .get("applyLayoutUsingDummies")
        .get(PARAMETERS)
        .get("dummyDp")
        .addGeneric("$NODE,$JS_BOOLEAN")
}

private fun fixGraphPartitionManager(source: Source) {
    val type = source.type("GraphPartitionManager")
    (type.optFlatMap(CONSTRUCTORS) + type.optFlatMap(METHODS))
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "partitionId" }
        .filter { it[TYPE] == IDATA_PROVIDER }
        .forEach { it.addGeneric("$GRAPH_OBJECT,$YID") }
}

private fun fixHierarchicLayoutCore(source: Source) {
    val methods = source.type("HierarchicLayoutCore")[METHODS]

    sequenceOf(
        "getEdgeLayoutDescriptors" to "$EDGE,yfiles.hierarchic.EdgeLayoutDescriptor",
        "getIncrementalHints" to "$GRAPH_OBJECT,$INCREMENTAL_HINT",
        "getNodeLayoutDescriptors" to "$NODE,yfiles.hierarchic.NodeLayoutDescriptor",
        "getSwimLaneDescriptors" to "$NODE,$SWIMLANE_DESCRIPTOR"
    ).forEach { (methodName, typeParameters) ->
        methods[methodName][RETURNS]
            .addGeneric(typeParameters)
    }
}

private fun fixDataProviders(source: Source) {
    source.type("DataProviders")
        .flatMap(STATIC_METHODS)
        .forEach {
            val name = it[NAME]

            val keyType = when {
                name == "createConstantDataProvider" || name == "createNegatedDataProvider" -> "K"
                "NodeDataProvider" in name -> NODE
                else -> EDGE
            }

            val valueType = when {
                name == "createNegatedDataProvider" -> JS_BOOLEAN
                "ForBoolean" in name -> JS_BOOLEAN
                "ForInt" in name -> JS_INT
                "ForNumber" in name -> JS_DOUBLE
                else -> "V"
            }

            it[TYPE_PARAMETERS] = mutableListOf<JSONObject>().apply {
                if (keyType == "K") {
                    add(typeParameter(keyType, JS_OBJECT))
                }

                if (valueType == "V") {
                    add(typeParameter(valueType, JS_OBJECT))
                }
            }.toList()

            it.flatMap(PARAMETERS)
                .forEach {
                    when (it[NAME]) {
                        "data",
                        "objectData" -> it[TYPE] = it[TYPE].replace(JS_OBJECT, valueType)
                        "nodeData" -> it.addGeneric("$NODE,$valueType")
                    }
                }

            it[RETURNS].addGeneric("$keyType,$valueType")
        }
}
