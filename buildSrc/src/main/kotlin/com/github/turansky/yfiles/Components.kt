package com.github.turansky.yfiles

internal fun Class.getComponents(): String? =
    when (classId) {
        "yfiles.algorithms.Point2D",
        "yfiles.algorithms.Rectangle2D",

        "yfiles.algorithms.YDimension",
        "yfiles.algorithms.YPoint",
        "yfiles.algorithms.YOrientedRectangle",

        "yfiles.algorithms.YInsets",

        "yfiles.geometry.Point",
        "yfiles.geometry.MutablePoint",

        "yfiles.geometry.Size",
        "yfiles.geometry.MutableSize",

        "yfiles.geometry.Rect",
        "yfiles.geometry.OrientedRectangle",

        "yfiles.geometry.Insets"
        -> constructorComponents()

        "yfiles.algorithms.YVector"
        -> components("x", "y")

        else -> null
    }

private fun Class.constructorComponents(): String =
    secondaryConstructors
        .asSequence()
        .maxBy { it.parameters.size }
        .let { it ?: primaryConstructor!! }
        .parameters
        .map { it.name }
        .let { components(*it.toTypedArray()) }

private fun Type.components(vararg properties: String): String =
    properties.asSequence()
        .mapIndexed { index, property -> "inline operator fun $classId.component${index + 1}() = $property" }
        .joinToString("\n")
