package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

internal const val DATA_TAG = "yfiles.graph.DataTag"

internal fun generateDataTagUtils(context: GeneratorContext) {
    // language=kotlin
    context[DATA_TAG] =
        """
            |package yfiles.graph
            |
            |external interface DataTag<K : Any, V : Any>
            |
            |fun <K : Any, V : Any> DataTag(source:String):DataTag<K,V> = 
            |    source.unsafeCast<DataTag<K,V>>()
        """.trimMargin()
}

internal fun applyDataTagHacks(source: Source) {
    val tagNames = setOf(
        "tag",
        "registryTag"
    )

    val typeParameterMap = mapOf(
        "createMapper" to "TData",
        "addRegistryInputMapper" to "TData",
        "addRegistryOutputMapper" to "TValue"
    )

    source.type("GraphMLIOHandler")
        .flatMap(METHODS)
        .forEach { method ->
            method.optFlatMap(PARAMETERS)
                .filter { it[NAME] in tagNames }
                .forEach {
                    val typeParameter = when {
                        method.has(TYPE_PARAMETERS) -> typeParameterMap.getValue(method[NAME])
                        else -> "*"
                    }

                    it[TYPE] = "$DATA_TAG<*, $typeParameter>"
                }
        }
}
