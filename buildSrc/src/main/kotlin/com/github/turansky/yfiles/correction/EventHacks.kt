package com.github.turansky.yfiles.correction

import org.json.JSONObject

internal fun applyEventHacks(source: Source) {
    source.types()
        .flatMap { it.optJsequence(CONSTRUCTORS) + it.optJsequence(METHODS) }
        .plus(source.functionSignatures.run { keySet().map { getJSONObject(it) } })
        .optFlatMap(PARAMETERS)
        .forEach {
            val name = it.getNewName()
                ?: return@forEach

            it[NAME] = name
        }
}

fun JSONObject.getNewName(): String? =
    when (get(NAME)) {
        "evt" -> "event"
        "args" -> if (get(TYPE).endsWith("Args")) {
            "event"
        } else {
            null
        }

        "src", "eventSource" -> "source"

        else -> null
    }
