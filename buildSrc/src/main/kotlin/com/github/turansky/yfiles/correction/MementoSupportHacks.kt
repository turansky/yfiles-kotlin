package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IMEMENTO_SUPPORT
import com.github.turansky.yfiles.between
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

    source.types()
        .filter { it[ID].run { startsWith("yfiles.graph.") && endsWith("Decorator") } }
        .optFlatMap(PROPERTIES)
        .filter { it[TYPE].endsWith("$IMEMENTO_SUPPORT>") }
        .forEach {
            val typeParameter = between(it[TYPE], "<", ",")
            it[TYPE] = it[TYPE].replace(">", "<$typeParameter,*>>")
        }

    source.functionSignatures
        .getJSONObject("yfiles.graph.MementoSupportProvider")
        .get(RETURNS)
        .addGeneric("T,*")
}
