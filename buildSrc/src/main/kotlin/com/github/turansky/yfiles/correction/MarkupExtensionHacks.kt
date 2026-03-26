package com.github.turansky.yfiles.correction

import org.json.JSONObject

internal fun fixMarkupExtensions(source: Source) {
    val basis = source.type("ColorExtension")
        .flatMap(METHODS)
        .first { it[NAME] == "provideValue" }

    source.types(
            "CompositeEdgeStyleExtension",
            "CompositeNodeStyleExtension",
            "CompositeLabelStyleExtension",
            "CompositePortStyleExtension"
    ).forEach {
        val obj = JSONObject(basis.toString(0))
        obj[ID] = "${it[NAME]}-method-provideValue"
        it.set(METHODS, listOf(obj))
    }
}