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
): JsonDelegate<List<T>> = ArrayDelegate(transform)

internal open class ArrayDelegate<T : Any>(
    private val transform: (JSONObject) -> T
) : JsonDelegate<List<T>>() {

    override fun read(
        source: JSONObject,
        key: String
    ): List<T> {
        if (!source.has(key)) {
            return emptyList()
        }

        val array = source.getJSONArray(key)
        val length = array.length()
        if (length == 0) {
            return emptyList()
        }

        return (0 until length)
            .asSequence()
            .map { array.getJSONObject(it) }
            .map(transform)
            .toList()
    }
}

internal fun stringList(
    transform: ((String) -> String)? = null
): JsonDelegate<List<String>> = StringArrayDelegate(transform)

internal class StringArrayDelegate(
    private val transform: ((String) -> String)? = null
) : JsonDelegate<List<String>>() {
    companion object {
        fun value(
            source: JSONObject,
            key: String,
            transform: ((String) -> String)? = null
        ): List<String> {
            if (!source.has(key)) {
                return emptyList()
            }

            val array = source.getJSONArray(key)
            val length = array.length()
            if (length == 0) {
                return emptyList()
            }

            val list = mutableListOf<String>()
            for (i in 0 until length) {
                list.add(array.getString(i))
            }

            return if (transform != null) {
                list.map(transform)
            } else {
                list.toList()
            }
        }
    }

    override fun read(
        source: JSONObject,
        key: String
    ): List<String> {
        return value(source, key, transform)
    }
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
