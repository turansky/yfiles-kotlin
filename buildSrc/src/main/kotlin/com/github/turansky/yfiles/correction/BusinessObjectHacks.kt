package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT

private const val BUSINESS_OBJECT = "yfiles.binding.BusinessObject"

internal fun generateBusinessObjectUtils(context: GeneratorContext) {
    // language=kotlin
    context["yfiles.binding.BusinessObject"] = "typealias BusinessObject = Any"
}

internal fun applyBusinessObjectHacks(source: Source) {
    source.types(
        "GraphBuilder",
        "TreeBuilder",
        "AdjacentNodesGraphBuilder"
    ).forEach {
        it.flatMap(PROPERTIES)
            .filter { it[NAME].endsWith("Source") }
            .filter { it[TYPE] == JS_ANY }
            .forEach { it[TYPE] = BUSINESS_OBJECT }

        it.flatMap(METHODS)
            .optFlatMap(PARAMETERS)
            .filter { it[NAME].run { endsWith("Object") || endsWith("Data") } }
            .filter { it[TYPE] == JS_OBJECT }
            .forEach { it[TYPE] = BUSINESS_OBJECT }

        it.method("getBusinessObject")[RETURNS][TYPE] = BUSINESS_OBJECT
    }
}
