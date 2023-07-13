package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.IEVENT_DISPATCHER
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import org.json.JSONObject

internal fun generateEventDispatcherUtils(context: GeneratorContext) {
    // language=kotlin
    context[IEVENT_DISPATCHER] = "external interface IEventDispatcher"
}

internal fun applyEventDispatcherHacks(source: Source) {
    source.types()
        .filter { it.has(EVENTS) || it[NAME] == "ItemModelManager" }
        .filterNot { it.hasParentDispatcher(source) }
        .forEach {
            if (it.has(IMPLEMENTS)) {
                it[IMPLEMENTS].put(IEVENT_DISPATCHER)
            } else {
                it[IMPLEMENTS] = arrayOf(IEVENT_DISPATCHER)
            }
        }

    val parameterNames = setOf(
        "sender",
        "source"
    )

    val likeObjectTypes = setOf(
        "this",
        JS_OBJECT,
        JS_ANY
    )

    source.types()
        .optFlatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] in parameterNames }
        .filter { it[TYPE] in likeObjectTypes }
        .forEach { it[TYPE] = IEVENT_DISPATCHER }

    source.functionSignatures.apply {
        keys().asSequence()
            .map { getJSONObject(it) }
            .optFlatMap(PARAMETERS)
            .filter { it[NAME] in parameterNames }
            .filter { it[TYPE] in likeObjectTypes }
            .forEach { it[TYPE] = IEVENT_DISPATCHER }
    }
}

private fun JSONObject.hasParentDispatcher(
    source: Source,
): Boolean {
    if (has(EXTENDS)) {
        val extends = getType(get(EXTENDS), source)
        if (extends.has(EVENTS) || extends.hasParentDispatcher(source)) {
            return true
        }
    }

    val implements = opt(IMPLEMENTS)
        ?: return false

    if (IEVENT_DISPATCHER in implements) {
        return true
    }

    return implements.asSequence()
        .map { it as String }
        .filterNot { it == IEVENT_DISPATCHER }
        .map { getType(it, source) }
        .any { it.has(EVENTS) || it.hasParentDispatcher(source) }
}

private fun getType(
    classId: String,
    source: Source,
): JSONObject {
    val className = classId
        .substringBefore("<")
        .substringAfterLast(".")

    return source.type(className)
}
