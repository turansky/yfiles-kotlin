package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GENERATED
import com.github.turansky.yfiles.JS_INT
import com.github.turansky.yfiles.RO
import com.github.turansky.yfiles.json.jObject
import org.json.JSONObject

internal fun addSizeExtensions(source: Source) {
    source.type("IEnumerable")
        .addGeneratedProperty("lastIndex", JS_INT, "size - 1")
}

private fun JSONObject.addGeneratedProperty(
    name: String,
    type: String,
    body: String
) {
    val property = jObject(
        NAME to name,
        TYPE to type,
        MODIFIERS to listOf(RO, GENERATED),
        BODY to body
    )

    get(PROPERTIES).put(property)
}


