package com.github.turansky.yfiles.correction

private const val RENDERER = "renderer"

internal fun applyStyleRendererHacks(source: Source) {
    source.types()
        .filter { isStyleLikeId(it[ID]) }
        .forEach { type ->
            val renderer = type.optFlatMap(PROPERTIES)
                .firstOrNull { it[NAME] == RENDERER }
                ?: return@forEach

            if (type[NAME].startsWith("Void")) {
                renderer[TYPE] = "${type[ID]}Renderer"
                return@forEach
            }

            type.optFlatMap(CONSTRUCTORS)
                .optFlatMap(PARAMETERS)
                .filter { it[NAME] == RENDERER }
                .map { it[TYPE] }
                .singleOrNull()
                ?.takeIf { isInterfaceLikeId(it) }
                ?.let { renderer[TYPE] = it }
        }
}

private fun isStyleLikeId(id: String): Boolean =
    id.startsWith("yfiles.styles.") &&
            (id.endsWith("Style") || id.endsWith("StyleBase"))

private fun isInterfaceLikeId(id: String): Boolean {
    val prefix = id.substringAfterLast(".").substring(0, 2)
    return prefix != prefix.uppercase()
}
