package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.correction.J_ID
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

private fun fixPackage(source: VsdxSource) {
    source.types()
        .forEach {
            val id = it.getString(J_ID)
            it.put(J_ID, "yfiles.$id")
        }

    source.functionSignatures.apply {
        keySet().toSet().forEach { id ->
            put("yfiles.$id", get(id))
        }
    }
}