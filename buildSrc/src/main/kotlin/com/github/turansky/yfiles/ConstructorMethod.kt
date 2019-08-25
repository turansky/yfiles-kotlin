package com.github.turansky.yfiles

internal fun Class.toConstructorMethodCode(): String? {
    if (!canHaveConstructorMethod()) {
        return null
    }

    if (primaryConstructor == null) {
        return null
    }

    if (secondaryConstructors.any { it.public }) {
        return null
    }

    if (!primaryConstructor.isConstructorMethodSource()) {
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
        extendedType() == null && properties.none { it.public && it.getterSetter } -> false
        else -> true
    }

private fun Constructor.isConstructorMethodSource(): Boolean =
    public && isDefault()