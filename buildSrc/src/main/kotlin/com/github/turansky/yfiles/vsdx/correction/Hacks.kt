package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.correction.J_IMPLEMENTS
import com.github.turansky.yfiles.correction.J_METHODS
import com.github.turansky.yfiles.correction.J_STATIC_METHODS
import org.json.JSONObject

internal fun applyVsdxHacks(api: JSONObject) {
    val source = VsdxSource(api)

    source.types()
        .onEach { it.remove(J_STATIC_METHODS) }
        .onEach { it.remove(J_METHODS) }
        .forEach { it.remove(J_IMPLEMENTS) }
}