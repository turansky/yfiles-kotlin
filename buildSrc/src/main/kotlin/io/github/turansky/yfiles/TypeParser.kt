package io.github.turansky.yfiles

internal object TypeParser {
    private val GENERIC_START = "<"
    private val GENERIC_END = ">"

    var standardTypeMap = emptyMap<String, String>()
    var javaArrayMode = false

    fun parse(type: String, signature: String?): String {
        return parseType(signature ?: type)
    }

    fun parseType(type: String): String {
        val standardType = standardTypeMap[type]
        if (standardType != null) {
            return standardType
        }

        if (!type.contains(GENERIC_START)) {
            return fixPackage(type)
        }

        val mainType = parseType(till(type, GENERIC_START))
        val parametrizedTypes = parseGenericParameters(between(type, GENERIC_START, GENERIC_END))
        val generics = checkGenericString(parametrizedTypes.byComma())

        if (javaArrayMode && mainType == "Array") {
            return "$generics[]"
        }

        return "$mainType<$generics>"
    }

    fun getGenericString(parameters: List<TypeParameter>): String {
        return if (parameters.isNotEmpty()) {
            checkGenericString("<${parameters.byComma { it.name }}> ")
        } else {
            ""
        }
    }

    private fun checkGenericString(generics: String): String {
        if (!javaArrayMode) {
            return generics
        }

        val result = generics
            .replace("boolean", "Boolean")
            .replace("double", "Double")
            .replace(", int", ", Integer")

        if (result == "int") {
            return "Integer"
        }

        return result
            .replace("int,", "Integer,")
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
}