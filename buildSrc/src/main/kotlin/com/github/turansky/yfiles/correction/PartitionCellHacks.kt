package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*

internal fun generatePartitionCellUtils(context: GeneratorContext) {
    context.resolve("yfiles/router/PartitionCellKey.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.router
                |
                |external interface PartitionCellKey<T:Any>
                |
                |fun <T:Any> PartitionCellKey(source:Any):PartitionCellKey<T> = 
                |    source.unsafeCast<PartitionCellKey<T>>()
            """.trimMargin()
        )
}

private val KEY_TYPE_MAP = mapOf(
    "EDGE_LABEL_CROSSING_COST_FACTORS_KEY" to "$ILIST<$JS_DOUBLE>",
    "EDGE_LABEL_LAYOUTS_KEY" to "$ILIST<$IEDGE_LABEL_LAYOUT>",
    "NODES_IN_NODE_TO_EDGE_DISTANCE_KEY" to "$ILIST<$NODE>",
    "NODES_KEY" to "$ILIST<$NODE>",
    "NODE_LABEL_CROSSING_COST_FACTORS_KEY" to "$ILIST<$JS_DOUBLE>",
    "NODE_LABEL_LAYOUTS_KEY" to "$ILIST<$INODE_LABEL_LAYOUT>",
    "PARTITION_GRID_CELL_ID_KEY" to "yfiles.layout.PartitionCellId",
    "PARTITION_GRID_COLUMN_INDEX_KEY" to JS_INT,
    "PARTITION_GRID_ROW_INDEX_KEY" to JS_INT
)

internal fun applyPartitionCellHacks(source: Source) {
    val methodNames = setOf(
        "getData",
        "putData",
        "removeData"
    )

    source.type("PartitionCell")
        .flatMap(METHODS)
        .filter { it[NAME] in methodNames }
        .onEach { it.setSingleTypeParameter(bound = JS_OBJECT) }
        .onEach { it[RETURNS][TYPE] = "T" }
        .onEach { it.changeNullability(true) }
        .flatMap(PARAMETERS)
        .forEach {
            it[TYPE] = when (val name = it[NAME]) {
                "key" -> "$PARTITION_CELL_KEY<T>"
                "data" -> "T"
                else -> throw IllegalArgumentException("Unable to calculate type by name '$name'")
            }
        }

    source.type("PartitionCellKeys")
        .flatMap(CONSTANTS)
        .forEach {
            val keyType = KEY_TYPE_MAP.getValue(it[NAME])
            it[TYPE] = "$PARTITION_CELL_KEY<$keyType>"
        }
}
