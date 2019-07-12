package com.github.turansky.yfiles.json

import org.json.JSONObject
import kotlin.reflect.KProperty

interface HasSource {
    val source: JSONObject
}

internal class ArrayDelegate<T>(private val transform: (JSONObject) -> T) {
    operator fun getValue(thisRef: HasSource, property: KProperty<*>): List<T> {
        val source = thisRef.source
        val key = property.name

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

internal class StringArrayDelegate {
    companion object {
        fun value(thisRef: HasSource, property: KProperty<*>): List<String> {
            val source = thisRef.source
            val key = property.name

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
            return list.toList()
        }
    }

    operator fun getValue(thisRef: HasSource, property: KProperty<*>): List<String> {
        return value(thisRef, property)
    }
}

internal class MapDelegate<T>(private val transform: (name: String, source: JSONObject) -> T) {

    operator fun getValue(thisRef: HasSource, property: KProperty<*>): Map<String, T> {
        val source = thisRef.source
        val key = property.name

        if (!source.has(key)) {
            return emptyMap()
        }

        val data = source.getJSONObject(key)
        val keys: List<String> = data.keySet()?.toList() ?: emptyList<String>()
        if (keys.isEmpty()) {
            return emptyMap()
        }

        return keys.associateBy({ it }, { transform(it, data.getJSONObject(it)) })
    }
}

internal class NullableStringDelegate {
    operator fun getValue(thisRef: HasSource, property: KProperty<*>): String? {
        val source = thisRef.source
        val key = property.name

        return if (source.has(key)) source.getString(key) else null
    }
}

internal class StringDelegate {
    companion object {
        fun value(thisRef: HasSource, property: KProperty<*>): String {
            return thisRef.source.getString(property.name)
        }
    }

    operator fun getValue(thisRef: HasSource, property: KProperty<*>): String {
        return value(thisRef, property)
    }
}

internal class BooleanDelegate {
    operator fun getValue(thisRef: HasSource, property: KProperty<*>): Boolean {
        val source = thisRef.source
        val key = property.name

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
