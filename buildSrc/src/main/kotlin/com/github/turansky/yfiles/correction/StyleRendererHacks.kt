package com.github.turansky.yfiles.correction

internal fun applyStyleRendererHacks(source: Source) {
    source.types()
        .filter { isStyleLikeId(it[ID]) }
        .forEach { type ->
            val renderer = type.optFlatMap(PROPERTIES)
                .firstOrNull { it[NAME] == "renderer" }
                ?: return@forEach

            if (type[NAME].startsWith("Void")) {
                renderer[TYPE] = "${type[ID]}Renderer"
            }
        }
}

private fun isStyleLikeId(id: String): Boolean =
    id.startsWith("yfiles.styles.") &&
            (id.endsWith("Style") || id.endsWith("StyleBase"))
