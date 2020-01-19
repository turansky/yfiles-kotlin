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
    source.type("GraphMLIOHandler")
}
