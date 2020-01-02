package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ELEMENT_ID
import com.github.turansky.yfiles.GeneratorContext

internal fun generateElementIdUtils(context: GeneratorContext) {
    // language=kotlin
    context[ELEMENT_ID] = """
            |package yfiles.graphml
            |
            |external interface ElementId
            |
            |fun ElementId(source:String):ElementId = 
            |    source.unsafeCast<ElementId>()
        """.trimMargin()
}

internal fun applyElementIdHacks(source: Source) {
    source.types()
        .filter { it[ID].startsWith("yfiles.graphml") }
}
