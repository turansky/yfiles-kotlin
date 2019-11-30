package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.YID
import org.json.JSONObject
import java.io.File

internal fun generateIdUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/lang/Id.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.lang
                |
                |external interface Id
                |
                |fun Id(source:Any):Id = 
                |    source.unsafeCast<Id>()
            """.trimMargin()
        )
}

private val ID_DP_KEYS = setOf(
    "yfiles.algorithms.EdgeDpKey<$JS_ANY>",
    "yfiles.algorithms.NodeDpKey<$JS_ANY>",

    "yfiles.algorithms.IEdgeLabelLayoutDpKey<$JS_ANY>",
    "yfiles.algorithms.INodeLabelLayoutDpKey<$JS_ANY>"
)

internal fun applyIdHacks(source: Source) {
    source.types()
        .flatMap { it.optJsequence(J_CONSTANTS) }
        .filter { it[J_TYPE] in ID_DP_KEYS }
        .filter { it[J_NAME].endsWith("_ID_DP_KEY") }
        .forEach {
            val newType = it[J_TYPE]
                .replace("<$JS_ANY>", "<$YID>")

            it.put(J_TYPE, newType)
        }

    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    typedItems(source)
        .filter { looksLikeId(it[J_NAME]) }
        .filter { it[J_TYPE] in likeObjectTypes }
        .forEach { it.put(J_TYPE, YID) }

    typedItems(source)
        .filter { it[J_NAME].endsWith("Ids") }
        .forEach {
            val newType = it[J_TYPE]
                .replace(",$JS_ANY>", ",$YID>")

            it.put(J_TYPE, newType)
        }

    source.type("BusRouterBusDescriptor")
        .jsequence(J_CONSTRUCTORS)
        .jsequence(J_PARAMETERS)
        .forEach {
            val name = it[J_NAME]
            if (name.endsWith("ID")) {
                it.put(J_NAME, name.replace("ID", "Id"))
            }
        }
}

private fun looksLikeId(name: String): Boolean =
    name == "id" || name.endsWith("Id") || name.endsWith("ID")

private fun typedItems(source: Source): Sequence<JSONObject> =
    source.types()
        .flatMap {
            (it.optJsequence(J_CONSTRUCTORS) + it.optJsequence(J_METHODS))
                .filter { it.has(J_PARAMETERS) }
                .jsequence(J_PARAMETERS)
                .plus(it.optJsequence(J_PROPERTIES))
        }
