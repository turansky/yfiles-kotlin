package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IVISUAL_TEMPLATE
import com.github.turansky.yfiles.JS_STRING
import com.github.turansky.yfiles.json.get
import java.io.File

private const val RESOURCE_KEY = "yfiles.view.ResourceKey"
private const val RESOURCE_MAP = "yfiles.view.ResourceMap"

private fun resourceKey(typeParameter: String) =
    "$RESOURCE_KEY<$typeParameter>"

internal fun generateResourceUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/view/Resources.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.view
                |
                |@JsName("String")
                |external class ResourceKey<T : Any> internal constructor()
                |
                |external interface ResourceMap {
                |    operator fun <T: Any> set(
                |       key: ResourceKey<T>, 
                |       value: T
                |    )
                |} 
            """.trimMargin()
        )
}

internal fun applyResourceHacks(source: Source) {
    source.types()
        .optFlatMap(CONSTANTS)
        .filter { it[TYPE] == JS_STRING }
        .filter { it[NAME].endsWith("_KEY") }
        .forEach { it[TYPE] = getType(it[NAME]) }

    source.type("CanvasComponent")[PROPERTIES]["resources"][TYPE] = RESOURCE_MAP

    source.type("DefaultPortCandidateDescriptor")[METHODS]["setTemplate"]
        .firstParameter[TYPE] = resourceKey(IVISUAL_TEMPLATE)

    source.type("RectangleIndicatorInstaller")
        .flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "resourceKey" }
        .single { it[TYPE] == JS_STRING }
        .also { it[TYPE] = resourceKey(IVISUAL_TEMPLATE) }
}

private fun getType(name: String): String {
    val typeParameter = when {
        name == "TEMPLATE_KEY" -> JS_STRING
        name.endsWith("_FILL_KEY") -> "yfiles.view.Fill"
        name.endsWith("STROKE_KEY") -> "yfiles.view.Stroke"
        else -> IVISUAL_TEMPLATE
    }

    return resourceKey(typeParameter)
}
