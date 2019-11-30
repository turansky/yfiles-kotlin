package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jArray
import org.json.JSONObject

internal fun applyDataHacks(source: Source) {
    fixGraph(source)

    fixDataProvider(source)
    fixDataAcceptor(source)
    fixDataMap(source)
    fixDataMaps(source)

    fixMethodTypes(source)
}

private val IDATA_PROVIDER = "yfiles.algorithms.IDataProvider"
private val IDATA_ACCEPTOR = "yfiles.algorithms.IDataAcceptor"
private val IDATA_MAP = "yfiles.algorithms.IDataMap"

private val MAP_INTERFACES = setOf(
    "yfiles.algorithms.IEdgeMap",
    "yfiles.algorithms.INodeMap"
)

private val DATA_PROVIDER_TYPE_MAP = mapOf(
    "IDataMap" to "K,V",
    "IEdgeMap" to "$EDGE,V",
    "INodeMap" to "$NODE,V",

    "DataProviderBase" to "K,V",
    "MapperDataProviderAdapter" to "TKey,TValue"
)

private val DATA_ACCEPTOR_TYPE_MAP = mapOf(
    "IDataMap" to "K,V",
    "IEdgeMap" to "$EDGE,V",
    "INodeMap" to "$NODE,V"
)

private val DATA_MAP_TYPE_MAP = mapOf(
    "IEdgeMap" to "$EDGE,V",
    "INodeMap" to "$NODE,V",

    "DataMapAdapter" to "K,V"
)

private fun fixGraph(source: Source) {
    val methods = source.type("Graph")
        .get(METHODS)

    methods.firstWithName("getDataProvider").apply {
        addKeyValueTypeParameters()
        firstParameter[TYPE] = "DpKeyBase<K,V>"

        get(RETURNS)
            .addGeneric("K,V")
    }

    methods.firstWithName("addDataProvider").apply {
        addKeyValueTypeParameters()
        firstParameter[TYPE] = "DpKeyBase<K,V>"

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

private fun fixDataProvider(source: Source) {
    source.type(IDATA_PROVIDER.substringAfterLast("."))
        .addKeyValueTypeParameters()

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it[TYPE] == IDATA_PROVIDER }
        .forEach { it[TYPE] = "$IDATA_PROVIDER<${it.getDataProviderTypeParameters()}>" }

    source.type("DataProviderBase")
        .addKeyValueTypeParameters()

    source.type("MapperDataProviderAdapter")
        .get(TYPE_PARAMETERS)
        .getJSONObject(1)
        .set(BOUNDS, arrayOf(JS_OBJECT))

    for ((className, typeParameters) in DATA_PROVIDER_TYPE_MAP) {
        source.type(className)
            .get(IMPLEMENTS)
            .apply { put(indexOf(IDATA_PROVIDER), "$IDATA_PROVIDER<$typeParameters>") }
    }
}

private fun JSONObject.getDataProviderTypeParameters(): String {
    val name = opt(NAME)
    return if (!has(DP_DATA) && name == "subtreeShapeProvider" || name == "nodeShapeProvider") {
        "$NODE,yfiles.tree.SubtreeShape"
    } else {
        getDefaultTypeParameters()
    }
}

private fun fixDataAcceptor(source: Source) {
    source.type(IDATA_ACCEPTOR.substringAfterLast("."))
        .addKeyValueTypeParameters()

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it[TYPE] == IDATA_ACCEPTOR }
        .forEach { it[TYPE] = "$IDATA_ACCEPTOR<${it.getDefaultTypeParameters()}>" }

    for ((className, typeParameters) in DATA_ACCEPTOR_TYPE_MAP) {
        source.type(className)
            .get(IMPLEMENTS)
            .apply { put(indexOf(IDATA_ACCEPTOR), "$IDATA_ACCEPTOR<$typeParameters>") }
    }
}

private fun fixDataMap(source: Source) {
    source.type(IDATA_MAP.substringAfterLast("."))
        .addKeyValueTypeParameters()

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it[TYPE] == IDATA_MAP }
        .forEach { it[TYPE] = "$IDATA_MAP<${it.getDataMapTypeParameters()}>" }

    for ((className, typeParameters) in DATA_MAP_TYPE_MAP) {
        source.type(className)
            .get(IMPLEMENTS)
            .apply { put(indexOf(IDATA_MAP), "$IDATA_MAP<$typeParameters>") }
    }
}

private fun JSONObject.getDataMapTypeParameters(): String =
    if (!has(DP_DATA) && opt(NAME) == "connectorMap") {
        "$NODE,yfiles.tree.ParentConnectorDirection"
    } else {
        getDefaultTypeParameters()
    }

private fun JSONObject.getDefaultTypeParameters(): String {
    if (!has(DP_DATA)) {
        return "*,*"
    }

    val name = get(NAME)
    return get(DP_DATA)
        .run {
            val keyType = getDefaultTypeParameter(name, get(DOMAIN)[TYPE])
            val valueType = getDefaultTypeParameter(name, get(VALUES)[TYPE])

            "$keyType,$valueType"
        }
}

private fun getDefaultTypeParameter(name: String, type: String): String =
    when (type) {
        JS_BOOLEAN -> type
        JS_NUMBER -> getDefaultNumberTypeParameter(name)
        JS_OBJECT -> getDefaultObjectTypeParameter(name)

        "yfiles.collections.IComparer<T>" -> "yfiles.collections.IComparer<*>"

        else -> type
    }

private fun getDefaultObjectTypeParameter(name: String): String =
    when (name) {
        "busIDAcceptor",
        "partitionIDDP" -> YID

        else -> "*"
    }

private fun getDefaultNumberTypeParameter(name: String): String =
    when (name) {
        "connectorMap" -> "yfiles.tree.ParentConnectorDirection"

        "eCapDP",
        "edgeLength",
        "keys",
        "layerId",
        "lCapDP",
        "minLength",
        "normalizedLayerId",
        "uCapDP",
        "w",
        "weight" -> INT

        "cost",
        "cost0DP",
        "costDP",
        "edgeCost",
        "edgeCosts",
        "edgeWeights",
        "heuristicCost",
        "supplyDP" -> DOUBLE

        else -> throw IllegalArgumentException("No type parameter for data map: $name")
    }

private fun fixDataMaps(source: Source) {
    MAP_INTERFACES.forEach {
        source.type(it.substringAfterLast("."))
            .setSingleTypeParameter("V", JS_OBJECT)
    }

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it[TYPE] in MAP_INTERFACES }
        .forEach {
            val typeParameter = it.getDataMapsTypeParameter()
            it[TYPE] = it[TYPE] + "<$typeParameter>"
        }

    source.type("Graph")
        .jsequence(PROPERTIES)
        .forEach { property ->
            val type = property[TYPE]
            MAP_INTERFACES.find { it in type }
                ?.also { property[TYPE] = type.replace(it, "$it<*>") }
        }
}

private fun JSONObject.getDataMapsTypeParameter(): String {
    if (!has(DP_DATA)) {
        return "*"
    }

    val type = get(DP_DATA)[VALUES][TYPE]

    return when (type) {
        JS_NUMBER -> getDataMapsNumberTypeParameter(get(NAME))
        JS_OBJECT -> when (get(NAME)) {
            "partitionIDMap" -> YID
            else -> type
        }
        else -> type
    }
}

private fun getDataMapsNumberTypeParameter(name: String): String =
    when (name) {
        "clusterIDs",
        "compNum",
        "compNumber",
        "dualsNM",
        "flowEM",
        "groupIDs",
        "intWeight",
        "layer",
        "layerID",
        "layerIDMap",
        "minLength",
        "rank",
        "result",
        "subtreeDepthMap",
        "subtreeSizeMap" -> INT

        "centrality",
        "closeness",
        "dist",
        "edgeCentrality",
        "map",
        "maxDist",
        "nodeCentrality" -> DOUBLE

        else -> throw IllegalArgumentException("No type parameter for data map: $name")
    }

private fun fixMethodTypes(source: Source) {
    source.types(
        "IDataProvider",
        "IDataAcceptor",

        "DataProviderBase",
        "DataMapAdapter",

        "MapperDataProviderAdapter"
    ).forEach {
        val typeParameters = it[TYPE_PARAMETERS]
        val keyTypeParameter = typeParameters.getJSONObject(0)[NAME]
        val valueTypeParameter = typeParameters.getJSONObject(1)[NAME]

        it.jsequence(METHODS)
            .forEach {
                val name = it[NAME]

                when (name) {
                    "get" -> it[RETURNS][TYPE] = valueTypeParameter

                    "set" -> it.get(PARAMETERS)
                        .firstWithName("value")
                        .set(TYPE, valueTypeParameter)
                }

                it.jsequence(PARAMETERS)
                    .filter { it[NAME] == "dataHolder" }
                    .forEach { it[TYPE] = keyTypeParameter }
            }
    }
}

private fun JSONObject.addKeyValueTypeParameters() {
    set(
        TYPE_PARAMETERS,
        jArray(
            typeParameter("K", JS_OBJECT),
            typeParameter("V", JS_OBJECT)
        )
    )
}

private fun JSONObject.getTypeHolders() =
    (optJsequence(CONSTRUCTORS) + optJsequence(STATIC_METHODS) + optJsequence(METHODS))
        .flatMap { it.optJsequence(PARAMETERS) + it.returnsSequence() }
        .plus(optJsequence(PROPERTIES))

private fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(RETURNS)) {
        sequenceOf(get(RETURNS))
    } else {
        emptySequence()
    }
