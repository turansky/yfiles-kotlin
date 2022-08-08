package com.github.turansky.yfiles

private const val GENERIC_START = "<"
private const val GENERIC_END = ">"

internal fun parse(
    type: String,
    signature: String?,
    documentedType: List<String>,
): String {
    val result = parseType(signature ?: type)
    if (documentedType.isEmpty())
        return result

    val comment = documentedType
        .map { it.replace("\n", "") }
        .joinToString(" | ")

    return "$result /* $comment */"
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

    if (type.endsWith("|null"))
        return parseType(type.removeSuffix("|null")) + "?"

    when (type) {
        "o is T",
        -> return "Boolean /* o is T */"

        "yfiles.layout.ItemMapping<yfiles.graph.INode,function(yfiles.graph.INode,yfiles.graph.INode):Double>",
        -> return "yfiles.layout.ItemMapping<yfiles.graph.INode,yfiles.lang.Func3<yfiles.graph.INode,yfiles.graph.INode,Int>>"

        "ItemMapping<IModelItem, IComparable|string|number|boolean>",
        -> return "yfiles.layout.ItemMapping<IModelItem, Comparable<*>>"

        "ItemMapping<INode, IComparable|string|number|boolean>",
        -> return "yfiles.layout.ItemMapping<INode, Comparable<*>>"
    }

    getKotlinType(type)?.let {
        return it
    }

    if (GENERIC_START !in type) {
        return type
    }

    val mainType = parseType(till(type, GENERIC_START))
    val parametrizedTypes = parseGenericParameters(type.between(GENERIC_START, GENERIC_END))
    val generics = parametrizedTypes.byComma()

    return "$mainType<$generics>"
}

// TODO: optimize calculation
private fun parseGenericParameters(parameters: String): List<String> {
    if (GENERIC_START !in parameters) {
        return parameters
            .split(",")
            .map { parseType(it) }
    }

    val result = mutableListOf<String>()

    var items = emptyList<String>()
    parameters.split(",").forEach { part ->
        items += part
        val str = items.joinToString(",")
        if (str.count { it == '<' } == str.count { it == '>' }) {
            result.add(parseType(str))
            items = emptyList()
        }
    }

    return result.toList()
}

internal fun String.asReadOnly(): String =
    replace("Array<", "ReadonlyArray<")
        .replace("$ICOLLECTION<", "$ICOLLECTION<out ")
        .replace("$ILIST<", "$ILIST<out ")

internal fun String.inMode(readOnly: Boolean): String =
    if (readOnly) {
        asReadOnly()
    } else {
        replace("Array<", "Array<in ")
            .replace("$ICOLLECTION<", "$ICOLLECTION<in ")
            .replace("$ILIST<", "$ILIST<in ")
    }
