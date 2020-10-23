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

        "yfiles.geometry.Insets",
        "yfiles.geometry.Tangent",

        "yfiles.view.Color"
        -> constructorComponents()

        "yfiles.algorithms.YVector"
        -> components("x", "y")

        else -> null
    }

internal fun Interface.getComponents(): String? =
    when (classId) {
        "yfiles.collections.IEnumerable"
        -> indexAccessComponents("elementAt")

        "yfiles.collections.IList",
        "yfiles.collections.IListEnumerable"
        -> indexAccessComponents("get")

        else -> null
    }

private fun Class.constructorComponents(): String =
    primaryConstructor!!
        .parameters
        .map { it.name }
        .let { components(*it.toTypedArray()) }

private fun Interface.indexAccessComponents(getMethod: String): String =
    (1..5).joinToString("\n") { index ->
        "inline operator fun ${generics.wrapperDeclaration} $classId${generics.asAliasParameters()}.component$index(): T = $getMethod(${index - 1})"
    }

private fun Class.components(vararg properties: String): String =
    properties.asSequence()
        .mapIndexed { index, property ->
            val type = memberProperties.first { it.name == property }.type
            """
                @JsName("__ygen_${property}_negy__")
                operator fun component${index + 1}(): $type
            """.trimIndent()
        }
        .joinToString("\n")
