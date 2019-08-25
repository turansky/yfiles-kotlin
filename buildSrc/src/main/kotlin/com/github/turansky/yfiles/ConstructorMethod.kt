package com.github.turansky.yfiles

internal fun Class.toConstructorMethodCode(): String? {
    if (abstract || generics.isNotEmpty()) {
        return null
    }

    if (primaryConstructor == null || secondaryConstructors.isNotEmpty()) {
        return null
    }

    if (!primaryConstructor.options || !primaryConstructor.isDefault()) {
        return null
    }

    if (extendedType() == null && properties.none { it.getterSetter }) {
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