package com.github.turansky.yfiles.correction

import org.json.JSONObject

internal fun applyEventHacks(source: Source) {
    source.types()
        .flatMap { it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS) }
        .plus(source.functionSignatures.run { keySet().map { getJSONObject(it) } })
        .optFlatMap(PARAMETERS)
        .forEach {
            val name = it.getNewName()
                ?: return@forEach

            it[NAME] = name
        }

    fixMouseEventArgsConstructor(source)
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

private fun fixMouseEventArgsConstructor(source: Source) {
    val nameFixMap = mapOf(
        "mouseWheelDelta" to "wheelDelta",
        "mouseWheelDeltaX" to "wheelDeltaX",
        "scrollType" to "deltaMode"
    )

    source.type("MouseEventArgs")
        .flatMap(CONSTRUCTORS)
        .flatMap(PARAMETERS)
        .filter { nameFixMap.containsKey(it[NAME]) }
        .forEach { it[NAME] = nameFixMap.getValue(it[NAME]) }
}
