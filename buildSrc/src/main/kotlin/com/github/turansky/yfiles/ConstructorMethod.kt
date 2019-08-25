package com.github.turansky.yfiles

internal fun Class.toConstructorMethodCode(): String? {
    if (!canHaveConstructorMethod()) {
        return null
    }

    if (primaryConstructor == null || secondaryConstructors.isNotEmpty()) {
        return null
    }

    if (!primaryConstructor.options || !primaryConstructor.isDefault()) {
        return null
    }

    return """
            |inline fun $name(
            |    block: $name.() -> Unit
            |): $name {
            |    return $name()
            |        .apply(block)
            |}
        """.trimMargin()
}

private fun Class.canHaveConstructorMethod(): Boolean =
    when {
        abstract -> false
        generics.isNotEmpty() -> false
        extendedType() == null && properties.none { it.getterSetter } -> false
        else -> true
    }