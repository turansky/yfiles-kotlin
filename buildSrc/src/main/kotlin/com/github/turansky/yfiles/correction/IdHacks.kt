package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.YID
import com.github.turansky.yfiles.json.get
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
        .flatMap { it.optFlatMap(CONSTANTS) }
        .filter { it[TYPE] in ID_DP_KEYS }
        .filter { it[NAME].endsWith("_ID_DP_KEY") }
        .forEach {
            val newType = it[TYPE]
                .replace("<$JS_ANY>", "<$YID>")

            it[TYPE] = newType
        }

    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    typedItems(source)
        .filter { looksLikeId(it[NAME]) }
        .filter { it[TYPE] in likeObjectTypes }
        .forEach { it[TYPE] = YID }

    typedItems(source)
        .filter { it[NAME].endsWith("Ids") }
        .forEach {
            val newType = it[TYPE]
                .replace(",$JS_ANY>", ",$YID>")

            it[TYPE] = newType
        }

    source.type("IEdgeData")[PROPERTIES].also { properties ->
        sequenceOf(
            "group",

            "sourceGroup",
            "targetGroup",

            "sourcePortGroup",
            "targetPortGroup"
        ).forEach { properties[it][TYPE] = YID }
    }

    source.type("BusRouterBusDescriptor")
        .flatMap(CONSTRUCTORS)
        .flatMap(PARAMETERS)
        .forEach {
            val name = it[NAME]
            if (name.endsWith("ID")) {
                it[NAME] = name.replace("ID", "Id")
            }
        }
}

private fun looksLikeId(name: String): Boolean =
    name == "id" || name.endsWith("Id") || name.endsWith("ID")

private fun typedItems(source: Source): Sequence<JSONObject> =
    source.types()
        .flatMap {
            (it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS))
                .optFlatMap(PARAMETERS)
                .plus(it.optFlatMap(PROPERTIES))
        }
