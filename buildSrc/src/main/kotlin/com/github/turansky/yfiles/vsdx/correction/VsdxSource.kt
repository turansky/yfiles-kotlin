package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

internal class VsdxSource(private val api: JSONObject) {
    val functionSignatures: JSONObject
        get() = api[FUNCTION_SIGNATURES]

    private val types: List<JSONObject> = api.flatMap(TYPES).toList()

    private val typeMap = types.associateBy { it.uid }

    fun types(): Sequence<JSONObject> =
        types.asSequence()

    fun type(className: String): JSONObject =
        typeMap.getValue(className)

    fun types(vararg classNames: String): Sequence<JSONObject> =
        classNames.asSequence()
            .map { type(it) }

    private val JSONObject.uid: String
        get() = opt(ES6_NAME) ?: get(NAME)
}
