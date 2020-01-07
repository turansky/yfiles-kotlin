package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IMEMENTO_SUPPORT
import com.github.turansky.yfiles.json.get

private const val T = "T"
private const val S = "S"

internal fun applyMementoSupportHacks(source: Source) {
    source.type("IMementoSupport").apply {
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

        get(METHODS)["getState"][RETURNS][TYPE] = S
    }

    fixDecoratorProperties(source, IMEMENTO_SUPPORT, true)

    source.functionSignatures
        .getJSONObject("yfiles.graph.MementoSupportProvider")
        .get(RETURNS)
        .addGeneric("T,*")
}
