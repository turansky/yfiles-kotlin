package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName

internal fun applyDpataHacks(source: Source) {
    fixGraph(source)
    fixLayoutGraphAdapter(source)
    fixTreeLayout(source)
    fixGraphPartitionManager(source)
    fixHierarchicLayoutCore(source)
}

private val GENERIC_DP_KEY = "yfiles.algorithms.DpKeyBase<K,V>"

private fun fixGraph(source: Source) {
    val methods = source.type("Graph")[METHODS]

    methods.firstWithName("getDataProvider").apply {
        setKeyValueTypeParameters()
        firstParameter[TYPE] = GENERIC_DP_KEY

        get(RETURNS)
            .addGeneric("K,V")
    }

    methods.firstWithName("addDataProvider").apply {
        setKeyValueTypeParameters()
        firstParameter[TYPE] = GENERIC_DP_KEY

        secondParameter.addGeneric("K,V")
    }

    sequenceOf("createEdgeMap", "createNodeMap")
        .map { methods.firstWithName(it) }
        .forEach {
            it.setSingleTypeParameter("V", JS_OBJECT)

            it[RETURNS]
                .addGeneric("V")
        }
}

private fun fixLayoutGraphAdapter(source: Source) {
    val methods = source.type("LayoutGraphAdapter")[METHODS]

    methods.firstWithName("getDataProvider").apply {
        setKeyValueTypeParameters()
        firstParameter[TYPE] = GENERIC_DP_KEY

        get(RETURNS)
            .addGeneric("K,V")
    }

    methods.firstWithName("addDataProvider").apply {
        get(PARAMETERS)
            .firstWithName("dataKey")
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
        properties.firstWithName(propertyName)
            .addGeneric("$EDGE,$valueType")
    }
}

private fun fixGraphPartitionManager(source: Source) {
    val type = source.type("GraphPartitionManager")
    (type.optJsequence(CONSTRUCTORS) + type.optJsequence(METHODS))
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "partitionId" }
        .filter { it[TYPE] == IDATA_PROVIDER }
        .forEach { it.addGeneric("$GRAPH_OBJECT,$YID") }
}

private fun fixHierarchicLayoutCore(source: Source) {
    val methods = source.type("HierarchicLayoutCore")[METHODS]

    sequenceOf(
        "getEdgeLayoutDescriptors" to "$EDGE,yfiles.hierarchic.EdgeLayoutDescriptor",
        "getIncrementalHints" to "$GRAPH_OBJECT,yfiles.hierarchic.IncrementalHint",
        "getNodeLayoutDescriptors" to "$NODE,yfiles.hierarchic.NodeLayoutDescriptor",
        "getSwimLaneDescriptors" to "$NODE,$SWIMLANE_DESCRIPTOR"
    ).forEach { (methodName, typeParameters) ->
        methods.firstWithName(methodName)
            .get(RETURNS)
            .addGeneric(typeParameters)
    }
}
