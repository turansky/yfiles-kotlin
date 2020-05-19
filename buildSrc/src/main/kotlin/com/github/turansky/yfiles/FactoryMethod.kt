package com.github.turansky.yfiles

private val HAS_FACTORY_METHOD = setOf(
    "Matrix",
    "GeneralPath",

    "GridNodePlacer",

    "DashStyle",
    "GraphComponent",
    "Stroke"
)

internal fun Class.toFactoryMethodCode(): String? {
    if (!hasFactoryMethod()) {
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

private fun Class.hasFactoryMethod(): Boolean {
    return when {
        name in HAS_FACTORY_METHOD -> true
        !canHaveFactoryMethod() -> false
        primaryConstructor == null -> false
        secondaryConstructors.any { it.public } -> false
        else -> primaryConstructor.isFactoryMethodSource()
    }
}

private fun Class.canHaveFactoryMethod(): Boolean =
    when {
        abstract -> false
        generics.isNotEmpty() -> false
        extendedType() == null && memberProperties.none { it.public && it.mode.writable } -> false
        else -> true
    }

private fun Constructor.isFactoryMethodSource(): Boolean =
    public && isDefault()
