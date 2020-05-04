package com.github.turansky.yfiles.json

import org.json.JSONObject
import kotlin.reflect.KProperty

interface HasSource {
    val source: JSONObject
}

abstract class JsonDelegate<T> {
    private var initialized = false
    private var value: T? = null

    abstract fun read(
        source: JSONObject,
        key: String
    ): T

    operator fun getValue(thisRef: HasSource, property: KProperty<*>): T {
        if (!initialized) {
            initialized = true
            value = read(thisRef.source, property.name)
        }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }
}

internal fun <T> delegate(
    read: (source: JSONObject, key: String) -> T
): JsonDelegate<T> = SimpleJsonDelegate(read)

internal fun <T : Any> named(
    create: (source: JSONObject) -> T
): JsonDelegate<T> = SimpleJsonDelegate { source, key ->
    create(source.getJSONObject(key))
}

internal fun <T : Any> optObject(
    create: (source: JSONObject) -> T
): JsonDelegate<T?> = SimpleJsonDelegate { source, key ->
    if (source.has(key)) {
        create(source.getJSONObject(key))
    } else {
        null
    }
}

private class SimpleJsonDelegate<T>(
    private val getData: (source: JSONObject, key: String) -> T
) : JsonDelegate<T>() {
    override fun read(
        source: JSONObject,
        key: String
    ): T = getData(source, key)
}

internal fun <T : Any> list(
    transform: (JSONObject) -> T
): JsonDelegate<List<T>> = delegate { source, key ->
    objectSequence(source, key)
        .map(transform)
        .toList()
}

internal fun <T : Comparable<T>> sortedList(
    transform: (JSONObject) -> T
): JsonDelegate<List<T>> = delegate { source, key ->
    objectSequence(source, key)
        .map(transform)
        .sorted()
        .toList()
}

private fun objectSequence(
    source: JSONObject,
    key: String
): Sequence<JSONObject> {
    if (!source.has(key)) {
        return emptySequence()
    }

    val array = source.getJSONArray(key)
    return (0 until array.length())
        .asSequence()
        .map(array::getJSONObject)
}

internal fun stringList(): JsonDelegate<List<String>> = delegate(::stringList)

internal fun stringList(
    transform: (String) -> String
): JsonDelegate<List<String>> = delegate { source, key ->
    stringList(source, key)
        .map(transform)
}

internal fun <T : Any> wrapStringList(
    wrap: (List<String>) -> T
): JsonDelegate<T> = delegate { source, key ->
    wrap(stringList(source, key))
}

private fun stringList(
    source: JSONObject,
    key: String
): List<String> {
    if (!source.has(key)) {
        return emptyList()
    }

    val array = source.getJSONArray(key)
    val length = array.length()
    if (length == 0) {
        return emptyList()
    }

    return (0 until length)
        .map(array::getString)
}

internal class MapDelegate<T>(
    private val transform: (name: String, source: JSONObject) -> T
) : JsonDelegate<Map<String, T>>() {
    override fun read(
        source: JSONObject,
        key: String
    ): Map<String, T> {
        if (!source.has(key)) {
            return emptyMap()
        }

        val data = source.getJSONObject(key)
        val keys: List<String> = data.keySet()?.toList() ?: emptyList()
        if (keys.isEmpty()) {
            return emptyMap()
        }

        return keys.associateWith {
            transform(it, data.getJSONObject(it))
        }
    }
}

internal fun optString(): JsonDelegate<String?> = delegate(::optString)

internal fun optString(
    source: JSONObject,
    key: String
): String? =
    source.optString(key, null)
        ?.takeIf { it.isNotEmpty() }

internal fun string(): JsonDelegate<String> = delegate(::string)

internal fun string(
    source: JSONObject,
    key: String
): String =
    source.getString(key)
        .apply { check(isNotEmpty()) }

internal fun int(): JsonDelegate<Int> = delegate { source, key ->
    source.getInt(key)
}

internal fun boolean(): JsonDelegate<Boolean> = delegate { source, key ->
    when (source.optString(key)) {
        "",
        "!1" -> false
        "!0" -> true
        else -> source.getBoolean(key)
    }
}
