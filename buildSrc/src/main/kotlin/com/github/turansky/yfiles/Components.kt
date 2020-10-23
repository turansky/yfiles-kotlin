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
        ILIST,
        ILIST_ENUMERABLE
        -> indexAccessComponents("get")

        else -> null
    }

internal fun Interface.getComponentExtensions(): String? =
    if (classId == IENUMERABLE) {
        indexAccessComponentExtensions("elementAt")
    } else {
        null
    }

private fun Class.constructorComponents(): String =
    primaryConstructor!!
        .parameters
        .map { it.name }
        .let { components(*it.toTypedArray()) }

private fun indexAccessComponents(getMethod: String): String =
    (1..5).joinToString("\n") { index ->
        """
            @JsName("__ygen_${getMethod}_${index - 1}_negy__")
            final operator fun component$index(): T
        """.trimIndent()
    }

private fun Interface.indexAccessComponentExtensions(getMethod: String): String =
    (1..5).joinToString("\n") { index ->
        "inline operator fun ${generics.wrapperDeclaration} $classId${generics.asAliasParameters()}.component$index(): T = $getMethod(${index - 1})"
    }

private fun Class.components(vararg properties: String): String =
    properties.asSequence()
        .mapIndexed { index, property ->
            val type = memberProperties.first { it.name == property }.type
            """
                /**
                 * @return [$property]
                 */    
                @JsName("__ygen_${property}_negy__")
                final operator fun component${index + 1}(): $type
            """.trimIndent()
        }
        .joinToString("\n")
