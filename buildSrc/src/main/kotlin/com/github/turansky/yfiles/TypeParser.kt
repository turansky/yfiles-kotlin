package com.github.turansky.yfiles

private val GENERIC_START = "<"
private val GENERIC_END = ">"

internal fun parse(type: String, signature: String?): String {
    return parseType(signature ?: type)
}

internal fun parseType(type: String): String {
    // TODO: remove class hack
    if (type.startsWith("yfiles.lang.Class<")) {
        return type
    }

    if (type == "yfiles.lang.Class") {
        return "yfiles.lang.Class<*>"
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

internal fun getGenericString(parameters: List<TypeParameter>): String {
    return if (parameters.isNotEmpty()) {
        "<${parameters.byComma { it.name }}> "
    } else {
        ""
    }
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