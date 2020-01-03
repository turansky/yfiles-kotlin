package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IMEMENTO_SUPPORT
import com.github.turansky.yfiles.between

internal fun applyMementoSupportHacks(source: Source) {
    val T = "T"
    val S = "S"

    source.type("IMementoSupport").apply {
        setTypeParameters(T, S)

        flatMap(METHODS)
            .flatMap(PARAMETERS)
            .onEach {
                val name = it[NAME]
                it[TYPE] = when {
                    name == "subject" -> T
                    name.startsWith("state") -> S
                    else -> TODO()
                }
            }
            .filter { it.has(RETURNS) }
            .forEach { it[RETURNS][TYPE] = T }
    }

    source.types()
        .filter { it[ID].run { startsWith("yfiles.graph.") && endsWith("Decorator") } }
        .optFlatMap(PROPERTIES)
        .filter { it[TYPE].endsWith("$IMEMENTO_SUPPORT>") }
        .forEach {
            val typeParameter = between(it[TYPE], "<", ",")
            it[TYPE] = it[TYPE].replace(">", "<$typeParameter,*>>")
        }
}
