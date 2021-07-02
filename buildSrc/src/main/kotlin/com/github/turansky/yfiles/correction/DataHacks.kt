package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

internal fun applyDataHacks(source: Source) {
    fixDataProvider(source)
    fixDataAcceptor(source)
    fixDataMap(source)
    fixDataMaps(source)

    fixMethodTypes(source)
}

private val MAP_INTERFACES = setOf(
    IEDGE_MAP,
    INODE_MAP
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

private fun fixDataProvider(source: Source) {
    source.type(IDATA_PROVIDER.substringAfterLast("."))
        .setKeyValueTypeParameters("in K", "out V", YOBJECT)

    source.types()
        .flatMap { it.getTypeHolders() }
        .filter { it[TYPE] == IDATA_PROVIDER }
        .forEach { it[TYPE] = "$IDATA_PROVIDER<${it.getDataProviderTypeParameters()}>" }

    source.type("DataProviderBase")
        .setKeyValueTypeParameters("in K", "out V", YOBJECT)

    source.type("MapperDataProviderAdapter")
        .setKeyValueTypeParameters("in TKey", "out TValue", YOBJECT)

    source.type("DataMapAdapter")
        .get(TYPE_PARAMETERS).getJSONObject(0)[BOUNDS] = arrayOf(YOBJECT)

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
        .setKeyValueTypeParameters("in K", "in V", YOBJECT)

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
        .setKeyValueTypeParameters("in K", "V", YOBJECT)

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

        "$ICOMPARER<T>" -> "$ICOMPARER<*>"

        else -> type
    }

private fun getDefaultObjectTypeParameter(name: String): String =
    when (name) {
        "busIDAcceptor",
        "partitionIDDP",
        -> YID

        "nodeType",
        -> INODE_TYPE

        else -> "*"
    }

private fun getDefaultNumberTypeParameter(name: String): String =
    when (name) {
        "connectorMap" -> "yfiles.tree.ParentConnectorDirection"
        "edgeDirectedness" -> EDGE_DIRECTEDNESS

        "eCapDP",
        "edgeLength",
        "keys",
        "layerId",
        "lCapDP",
        "minLength",
        "normalizedLayerId",
        "uCapDP",
        "w",
        "weight",
        "initialLabel",
        "communityIndex",
        -> INT

        "cost",
        "cost0DP",
        "costDP",
        "edgeCost",
        "edgeCosts",
        "edgeWeights",
        "nodeWeight",
        "edgeWeight",
        "heuristicCost",
        "supplyDP",
        "initialPageRank",
        -> DOUBLE

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
        .forEach { it.addGeneric(it.getDataMapsTypeParameter()) }

    source.type("Graph")
        .flatMap(PROPERTIES)
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

    return when (val type = get(DP_DATA)[VALUES][TYPE]) {
        JS_NUMBER -> getDataMapsNumberTypeParameter(get(NAME))
        JS_OBJECT -> when (get(NAME)) {
            "partitionIDMap" -> YID
            "markMap" -> BIPARTITION_MARK
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
        "subtreeSizeMap",
        "kValue",
        "finalLabel",
        "communityIndex",
        -> INT

        "centrality",
        "closeness",
        "dist",
        "edgeCentrality",
        "map",
        "maxDist",
        "nodeCentrality",
        "centralityMap",
        "pageRank",
        "coefficientMap",
        -> DOUBLE

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
        val keyTypeParameter = it.getTypeParameterName(0)
        val valueTypeParameter = it.getTypeParameterName(1)

        it.flatMap(METHODS)
            .forEach {
                when (it[NAME]) {
                    "get" -> it[RETURNS][TYPE] = valueTypeParameter

                    "set" -> it[PARAMETERS]["value"]
                        .set(TYPE, valueTypeParameter)
                }

                it.flatMap(PARAMETERS)
                    .filter { it[NAME] == "dataHolder" }
                    .forEach { it[TYPE] = keyTypeParameter }
            }
    }
}

private fun JSONObject.getTypeParameterName(index: Int): String =
    get(TYPE_PARAMETERS)
        .getJSONObject(index)[NAME]
        .removePrefix("in ")
        .removePrefix("out ")

private fun JSONObject.getTypeHolders(): Sequence<JSONObject> =
    (optFlatMap(CONSTRUCTORS) + optFlatMap(METHODS))
        .flatMap { it.optFlatMap(PARAMETERS) + it.returnsSequence() }
        .plus(optFlatMap(PROPERTIES))

