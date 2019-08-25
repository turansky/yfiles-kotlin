package com.github.turansky.yfiles

internal fun Class.toConstructorMethodCode(): String? {
    if (!hasConstructorMethod()) {
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

private fun Class.hasConstructorMethod(): Boolean {
    return when {
        !canHaveConstructorMethod() -> false
        primaryConstructor == null -> false
        secondaryConstructors.any { it.public } -> false
        else -> primaryConstructor.isConstructorMethodSource()
    }
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