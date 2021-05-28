package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.between

internal fun fixDecoratorProperties(
    source: Source,
    type: String,
    addExtraTypeParameter: Boolean = false,
) {
    source.types()
        .filter { it[ID].run { startsWith("yfiles.graph.") && endsWith("Decorator") } }
        .optFlatMap(PROPERTIES)
        .filter { it[TYPE].endsWith("$type>") }
        .forEach {
            var typeParameters = it[TYPE].between("<", ",")
            if (addExtraTypeParameter) {
                typeParameters += ",*"
            }
            it.replaceInType(">", "<$typeParameters>>")
        }
}
