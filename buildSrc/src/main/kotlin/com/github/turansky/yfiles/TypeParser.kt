package com.github.turansky.yfiles

private val GENERIC_START = "<"
private val GENERIC_END = ">"

internal fun parse(type: String, signature: String?): String {
    return parseType(signature ?: type)
}

internal fun parseType(type: String): String {
    // TODO: remove class hack
    if (type.startsWith("$YCLASS<")) {
        return type
    }

    if (type == YCLASS) {
        return "$YCLASS<*>"
    }

    if (type.startsWith("$ICOMPARABLE<")) {
        return type
    }

    if (type == ICOMPARABLE) {
        return "$ICOMPARABLE<*>"
    }

    // TODO: remove after generic support finish
    if (type.startsWith("$YLIST<")) {
        return type
    }

    if (type == YLIST) {
        return "$YLIST<*>"
    }

    getKotlinType(type)?.let {
        return it
    }

    if (!type.contains(GENERIC_START)) {
        return fixPackage(type)
    }

    val mainType = parseType(till(type, GENERIC_START))
    val parametrizedTypes = parseGenericParameters(between(type, GENERIC_START, GENERIC_END))
    val generics = parametrizedTypes.byComma()

    return "$mainType<$generics>"
}

// TODO: optimize calculation
private fun parseGenericParameters(parameters: String): List<String> {
    if (!parameters.contains(GENERIC_START)) {
        return parameters
            .split(",")
            .map { parseType(it) }
    }

    val result = mutableListOf<String>()

    var items = emptyList<String>()
    parameters.split(",").forEach { part ->
        items += part
        val str = items.joinToString(",")
        if (str.count { it.equals('<') } == str.count { it.equals('>') }) {
            result.add(parseType(str))
            items = emptyList()
        }
    }

    return result.toList()
}