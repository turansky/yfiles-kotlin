package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

internal fun applyDpataHacks(source: Source) {
    fixGraph(source)
    fixDefaultLayoutGraph(source)
    fixDfs(source)
    fixLayoutGraphAdapter(source)
    fixTreeLayout(source)
    fixGraphPartitionManager(source)
    fixHierarchic(source)
    fixTriangulator(source)
    fixYGraphAdapter(source)
    fixMISLabelingBase(source)
    fixParallelEdgeRouter(source)

    fixDataProviders(source)
    fixMaps(source)
    fixLists(source)
    fixComparers(source)
}

private const val GENERIC_DP_KEY = "$DP_KEY_BASE<K,V>"

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

    methods["removeDataProvider"].firstParameter[TYPE] = "$DP_KEY_BASE<*,*>"

    sequenceOf("createEdgeMap", "createNodeMap")
        .map { methods[it] }
        .forEach {
            it.setSingleTypeParameter("V", JS_OBJECT)

            it[RETURNS]
                .addGeneric("V")
        }
}

private fun fixDefaultLayoutGraph(source: Source) {
    val properties = source.type("DefaultLayoutGraph")[PROPERTIES]

    properties["nodeLabelMap"].addGeneric("Array<$INODE_LABEL_LAYOUT>")
    properties["edgeLabelMap"].addGeneric("Array<$IEDGE_LABEL_LAYOUT>")

    properties["nodeLabelFeatureMap"]
        .also { it[TYPE] = it[TYPE].replace("$JS_ANY,$JS_ANY", "$INODE_LABEL_LAYOUT,$NODE") }

    properties["edgeLabelFeatureMap"]
        .also { it[TYPE] = it[TYPE].replace("$JS_ANY,$JS_ANY", "$IEDGE_LABEL_LAYOUT,$EDGE") }
}

private fun fixDfs(source: Source) {
    source.type("DfsAlgorithm")
        .property("stateMap")
        .addGeneric(DFS_STATE)
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
        .method("applyLayoutUsingDummies")
        .get(PARAMETERS)
        .get("dummyDp")
        .addGeneric("$NODE,$JS_BOOLEAN")

    source.type("LeftRightNodePlacer")
        .method("createLeftRightDataProvider")
        .also {
            it.firstParameter.addGeneric("$NODE,yfiles.tree.INodePlacer")
            it[RETURNS].addGeneric("$NODE,$JS_BOOLEAN")
        }
}

private fun fixGraphPartitionManager(source: Source) {
    val type = source.type("GraphPartitionManager")
    (type.optFlatMap(CONSTRUCTORS) + type.optFlatMap(METHODS))
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "partitionId" }
        .filter { it[TYPE] == IDATA_PROVIDER }
        .forEach { it.addGeneric("$GRAPH_OBJECT,$YID") }
}

private fun fixHierarchic(source: Source) {
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

    source.type("PortCandidateOptimizer")
        .method("getPortCandidateSetDataProvider")
        .get(RETURNS)
        .addGeneric("$NODE,yfiles.layout.PortCandidateSet")

    source.type("WeightedLayerer")
        .property("weight")
        .addGeneric("$EDGE,$JS_INT")

    source.type("SimplexNodePlacer")
        .method("assignNodesToSublayer")
        .parameter("lowerSublayer")
        .addGeneric("yfiles.hierarchic.ILayer")
}

private fun fixTriangulator(source: Source) {
    source.type("TriangulationAlgorithm")
        .flatMap(METHODS)
        .filter { STATIC in it[MODIFIERS] }
        .flatMap(PARAMETERS)
        .forEach {
            when (it[NAME]) {
                "pointData" -> it.addGeneric("$NODE,$YPOINT")
                "revMap", "reverseEdgeMap" -> it.addGeneric(EDGE)
                "resultMap" -> it.addGeneric(YPOINT)
            }
        }
}

private fun fixYGraphAdapter(source: Source) {
    val dataNames = setOf(
        "createDataMap",
        "createDataProvider"
    )

    val graphDataNames = setOf(
        "createEdgeMap",
        "createNodeMap"
    )

    val mapperNames = setOf(
        "createEdgeMapper",
        "createNodeMapper"
    )

    source.type("YGraphAdapter").also {
        it.flatMap(METHODS)
            .filter { it[NAME] in dataNames }
            .map { it[RETURNS] }
            .forEach { it.addGeneric("K,V") }

        it.method("createMapper").also {
            it.strictBound("T")
            it.firstParameter.addGeneric("$GRAPH_OBJECT,T")
        }

        it.flatMap(METHODS)
            .filter { it[NAME] in graphDataNames }
            .onEach { it.strictBound("V") }
            .map { it[RETURNS] }
            .forEach { it.addGeneric("V") }

        it.flatMap(METHODS)
            .filter { it[NAME] in mapperNames }
            .onEach { it.strictBound("T") }
            .map { it.firstParameter }
            .forEach { it.addGeneric("T") }
    }
}

private fun fixMISLabelingBase(source: Source) {
    source.type("MISLabelingBase").also {
        it.property("nodesToBoxes")
            .addGeneric("yfiles.layout.LabelCandidate")

        it.property("nodesToID")
            .addGeneric(YID)

        it.property("boxesToNodes")
            .also { it[TYPE] = it[TYPE].replace("$JS_ANY,$JS_ANY", "yfiles.layout.LabelCandidate,$NODE") }

        it.method("assignProfit")
            .get(RETURNS)
            .addGeneric(JS_DOUBLE)
    }
}

private fun fixParallelEdgeRouter(source: Source) {
    source.type("ParallelEdgeRouter")
        .property("parallelEdges")
        .addGeneric(EDGE)
}

private fun fixDataProviders(source: Source) {
    source.type("DataProviders")
        .flatMap(METHODS)
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

            if (name == "createNegatedDataProvider") {
                it.firstParameter.addGeneric("$keyType,$valueType")
            } else {
                it.flatMap(PARAMETERS)
                    .forEach {
                        when (it[NAME]) {
                            "data", "objectData" -> it[TYPE] = it[TYPE].replace(JS_OBJECT, valueType)
                            "nodeData" -> it.addGeneric("$NODE,$valueType")
                        }
                    }
            }

            it[RETURNS].addGeneric("$keyType,$valueType")
        }
}

private fun fixMaps(source: Source) {
    source.type("Maps")
        .flatMap(METHODS)
        .forEach {
            val returns = it[RETURNS]

            val keyType = when (returns[TYPE]) {
                IDATA_MAP -> "K"
                INODE_MAP -> NODE
                IEDGE_MAP -> EDGE
                else -> return@forEach
            }

            val name = it[NAME]
            val valueType = when {
                "ForBoolean" in name -> JS_BOOLEAN
                "ForInt" in name || "IntMap" in name -> JS_INT
                "ForNumber" in name || "DoubleMap" in name -> JS_DOUBLE
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

            val typeParameters = "$keyType,$valueType"
            it.optFlatMap(PARAMETERS)
                .forEach {
                    when (it[NAME]) {
                        "map" -> it[TYPE] = it[TYPE].replace("$JS_OBJECT,$JS_OBJECT", typeParameters)
                        "defaultValue" -> if (it[TYPE] == JS_OBJECT) it[TYPE] = "V"
                        "data", "objectData" -> it[TYPE] = it[TYPE].replace("<$JS_OBJECT>", "<V>")
                        else -> when (it[TYPE]) {
                            IDATA_PROVIDER,
                            IDATA_ACCEPTOR,
                            IDATA_MAP -> it.addGeneric(typeParameters)
                        }
                    }
                }

            it[RETURNS].addGeneric(if (keyType == "K") typeParameters else valueType)
        }
}

private fun fixLists(source: Source) {
    sequenceOf(
        "YList" to "T",
        "YNodeList" to NODE,
        "EdgeList" to EDGE
    ).forEach { (className, keyType) ->
        source.type(className)
            .flatMap(CONSTRUCTORS)
            .optFlatMap(PARAMETERS)
            .filter { it[NAME] == "predicate" }
            .forEach { it.addGeneric("$keyType,$JS_BOOLEAN") }
    }
}

private fun fixComparers(source: Source) {
    source.type("Comparers")
        .flatMap(METHODS)
        .filter { it.has(PARAMETERS) }
        .filter { it.firstParameter[TYPE] == IDATA_PROVIDER }
        .forEach {
            val name = it[NAME]

            val nodeProxy = "Source" in name || "Target" in name
            val keyType = if (nodeProxy) EDGE else "K"

            val valueType = when {
                "IntData" in name -> JS_INT
                "NumberData" in name -> JS_DOUBLE
                else -> "V"
            }

            val valueBound = if (name == "createComparableDataComparer") {
                "yfiles.lang.IComparable<V>"
            } else {
                JS_OBJECT
            }

            it[TYPE_PARAMETERS] = mutableListOf<JSONObject>().apply {
                if (keyType == "K") {
                    add(typeParameter(keyType, JS_OBJECT))
                }

                if (valueType == "V") {
                    add(typeParameter(valueType, valueBound))
                }
            }.toList()

            val parameterKeyType = if (nodeProxy) NODE else keyType
            it.firstParameter.addGeneric("$parameterKeyType,$valueType")
            it[RETURNS].also {
                it[TYPE] = it[TYPE].substringBefore("<") + "<$keyType>"
            }
        }
}

private fun JSONObject.strictBound(name: String) {
    get(TYPE_PARAMETERS)[name][BOUNDS] = arrayOf(JS_OBJECT)
}
