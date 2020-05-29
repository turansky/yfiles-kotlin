package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IMEMENTO_SUPPORT

private const val T = "T"
private const val S = "S"

internal fun applyMementoSupportHacks(source: Source) {
    source.type("IMementoSupport") {
        setTypeParameters("in $T", S)

        flatMap(METHODS)
            .flatMap(PARAMETERS)
            .forEach {
                val name = it[NAME]
                it[TYPE] = when {
                    name == "subject" -> T
                    name.startsWith("state") -> S
                    else -> TODO()
                }
            }

        method("getState")[RETURNS][TYPE] = S
    }

    fixDecoratorProperties(source, IMEMENTO_SUPPORT, true)

    source.functionSignature("yfiles.graph.MementoSupportProvider")
        .get(RETURNS)
        .addGeneric("T,*")
}
