package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY

internal const val INCREMENTAL_HINT = "yfiles.hierarchic.IncrementalHint"

internal fun generateIncrementalHint(context: GeneratorContext) {
    // language=kotlin
    context[INCREMENTAL_HINT] = """
            @JsName("Object")
            external class IncrementalHint
            private constructor()
        """.trimIndent()
}

internal fun applyIncrementalHintHacks(source: Source) {
    source.type("IIncrementalHintsFactory")
        .flatMap(METHODS)
        .forEach { it[RETURNS][TYPE] = INCREMENTAL_HINT }

    source.type("IncrementalHintItemMapping").also {
        it[EXTENDS] = it[EXTENDS].replace(",$JS_ANY,", ",$INCREMENTAL_HINT,")
    }

    source.types("HierarchicLayout", "HierarchicLayoutCore")
        .map { it.constant("INCREMENTAL_HINTS_DP_KEY") }
        .forEach { it.replaceInType("<$JS_ANY>", "<$INCREMENTAL_HINT>") }

    source.type("INodeData")
        .property("incrementalHint")[TYPE] = INCREMENTAL_HINT
}
