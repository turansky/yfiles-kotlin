package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ANY
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import org.json.JSONObject

internal fun applyYListHacks(source: Source) {
    fixYList(source)
}

private fun fixYList(source: Source) {
    source.type("YList")
        .fixGeneric()

    source.type("YNodeList")
        .addExtendsGeneric(ANY)

    source.type("EdgeList")
        .addExtendsGeneric(ANY)
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter(bound = JS_ANY)

    getJSONArray(J_IMPLEMENTS).apply {
        put(0, getString(0).replace("<$JS_ANY>", "<T>"))
    }

    (jsequence(J_CONSTRUCTORS) + jsequence(J_METHODS))
        .flatMap { it.optJsequence(J_PARAMETERS) + it.returnsSequence() }
        .plus(jsequence(J_PROPERTIES))
        .forEach {
            val type = it.getString(J_TYPE)
            val newType = if (type == JS_ANY || type == JS_OBJECT) {
                "T"
            } else {
                type.replace("<$JS_ANY>", "<T>")
                    .replace("<$JS_OBJECT>", "<T>")
            }
            it.put(J_TYPE, newType)
        }
}

private fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(J_RETURNS)) {
        sequenceOf(getJSONObject(J_RETURNS))
    } else {
        emptySequence()
    }