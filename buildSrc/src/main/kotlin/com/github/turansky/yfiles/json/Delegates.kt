package com.github.turansky.yfiles.json

import org.json.JSONObject
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal interface HasSource {
    val source: JSONObject
}

internal typealias Prop<T> = ReadOnlyProperty<HasSource, T>

internal abstract class PropDelegate<T> : Prop<T> {
    private var initialized = false
    private var value: T? = null

    abstract fun read(
        source: JSONObject,
        key: String
    ): T

    override operator fun getValue(
        thisRef: HasSource,
        property: KProperty<*>
    ): T {
        if (!initialized) {
            initialized = true
            value = read(thisRef.source, property.name)
        }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }
}

private fun <T> prop(
    read: (source: JSONObject, key: String) -> T
): Prop<T> = SimplePropDelegate(read)

private fun <T, R> prop(
    read: (source: JSONObject, key: String) -> T,
    transform: T.() -> R
): Prop<R> =
    prop { source, key ->
        read(source, key).transform()
    }

internal fun <T : Any> named(
    create: (source: JSONObject) -> T
): Prop<T> = prop(JSONObject::getJSONObject, create)

internal fun <T : Any> optNamed(
    create: (source: JSONObject) -> T
): Prop<T?> = prop { source, key ->
    if (source.has(key)) {
        create(source.getJSONObject(key))
    } else {
        null
    }
}

internal fun <T : Any> optNamed(
    name: String,
    create: (source: JSONObject) -> T?
): Prop<T?> = prop { source, _ ->
    if (source.has(name)) {
        create(source.getJSONObject(name))
    } else {
        null
    }
}

private class SimplePropDelegate<T>(
    private val getData: (source: JSONObject, key: String) -> T
) : PropDelegate<T>() {
    override fun read(
        source: JSONObject,
        key: String
    ): T = getData(source, key)
}

internal fun <T : Any> list(
    transform: (JSONObject) -> T
): Prop<List<T>> =
    prop(::objectSequence) {
        map(transform).toList()
    }

internal fun <T : Comparable<T>> sortedList(
    transform: (JSONObject) -> T
): Prop<List<T>> =
    prop(::objectSequence) {
        map(transform).sorted().toList()
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

internal fun stringList(): Prop<List<String>> = prop(::stringList)

internal fun stringList(
    transform: (String) -> String
): Prop<List<String>> =
    prop(::stringList) {
        map(transform)
    }

internal fun <T : Any> wrapStringList(
    wrap: (List<String>) -> T
): Prop<T> =
    prop(::stringList, wrap)

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

internal fun optString(): Prop<String?> =
    prop(::optString)

internal fun optString(
    source: JSONObject,
    key: String
): String? =
    source.optString(key, null)
        ?.takeIf { it.isNotEmpty() }

internal fun string(): Prop<String> =
    prop(::string)

internal fun string(
    transform: (String) -> String
): Prop<String> =
    prop(::string, transform)

private fun string(
    source: JSONObject,
    key: String
): String =
    source.getString(key)
        .apply { check(isNotEmpty()) }

internal fun int(): Prop<Int> =
    prop(JSONObject::getInt)

internal fun boolean(): Prop<Boolean> = prop { source, key ->
    when (source.optString(key)) {
        "",
        "!1" -> false
        "!0" -> true
        else -> source.getBoolean(key)
    }
}
