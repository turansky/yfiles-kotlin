package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.get
import org.json.JSONObject

internal fun generateIdUtils(context: GeneratorContext) {
    // language=kotlin
    context[YID] = """
            external interface Id
            
            inline fun Id(source:Any):Id = 
                source.unsafeCast<Id>()
        """.trimIndent()
}

internal fun applyIdHacks(source: Source) {
    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    typedItems(source)
        .filter { looksLikeId(it[NAME]) }
        .filter { it[TYPE] in likeObjectTypes }
        .forEach { it[TYPE] = YID }

    typedItems(source)
        .filter { it[NAME].let { it.endsWith("Ids") || it == "customGroups" } }
        .forEach {
            val newType = it[TYPE]
                .replace(",$JS_ANY>", ",$YID>")

            it[TYPE] = newType
        }

    source.types("GraphClipboard", "IClipboardIdProvider")
        .map { it.method("getId") }
        .forEach { it[RETURNS][TYPE] = YID }

    source.type("ChannelRoutingTool")
        .flatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "key" }
        .filter { it[TYPE] in likeObjectTypes }
        .forEach { it[TYPE] = YID }
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
