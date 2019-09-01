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

internal open class ArrayDelegate<T>(
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
            for (i in 0..length - 1) {
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

        return keys.associateBy({ it }, { transform(it, data.getJSONObject(it)) })
    }
}

internal fun optString(): JsonDelegate<String?> = NullableStringDelegate()

internal class NullableStringDelegate : JsonDelegate<String?>() {
    companion object {
        fun value(
            source: JSONObject,
            key: String
        ): String? {
            if (source.has(key)) {
                val value = source.getString(key)
                if (value.isNotEmpty()) {
                    return value
                }
            }

            return null
        }
    }

    override fun read(
        source: JSONObject,
        key: String
    ): String? =
        value(source, key)
}

internal fun string(): JsonDelegate<String> = StringDelegate()

internal class StringDelegate : JsonDelegate<String>() {
    companion object {
        fun value(
            source: JSONObject,
            key: String
        ): String =
            source.getString(key)
    }

    override fun read(
        source: JSONObject,
        key: String
    ): String =
        value(source, key)
}

internal fun boolean(): JsonDelegate<Boolean> = BooleanDelegate()

internal class BooleanDelegate : JsonDelegate<Boolean>() {
    override fun read(
        source: JSONObject,
        key: String
    ): Boolean {
        if (!source.has(key)) {
            return false
        }

        val value = source.getString(key)
        return when (value) {
            "!0" -> true
            "!1" -> false
            else -> source.getBoolean(key)
        }
    }
}
