package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.JS_ANY

internal fun applyComparerHacks(source: Source) {
    fixComparerInheritors(source)
}

private fun fixComparerInheritors(source: Source) {
    sequenceOf(
        "DefaultOutEdgeComparer" to "yfiles.algorithms.Edge",
        "NodeOrderComparer" to "yfiles.algorithms.Node",
        "NodeWeightComparer" to "yfiles.algorithms.Node"
    ).forEach { (className, generic) ->
        source.type(className)
            .apply {
                getJSONArray(J_IMPLEMENTS).apply {
                    require(length() == 1)
                    require(get(0) == "yfiles.collections.IComparer<$JS_ANY>")

                    put(0, "yfiles.collections.IComparer<$generic>")
                }

                jsequence(J_METHODS)
                    .filter { it.getString(J_NAME) == "compare" }
                    .jsequence(J_PARAMETERS)
                    .forEach { it.put(J_TYPE, generic) }
            }
    }
}