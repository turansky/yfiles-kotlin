package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.IVISUAL_TEMPLATE
import com.github.turansky.yfiles.JS_STRING

private const val RESOURCE_KEY = "yfiles.view.ResourceKey"
private const val RESOURCE_MAP = "yfiles.view.ResourceMap"

private fun resourceKey(typeParameter: String) =
    "$RESOURCE_KEY<$typeParameter>"

internal fun generateResourceUtils(context: GeneratorContext) {
    // language=kotlin
    context["yfiles.view.Resources"] = """
            |@JsName("String")
            |sealed external class ResourceKey<T : Any>
            |
            |external interface ResourceMap {
            |    operator fun <T: Any> set(
            |       key: ResourceKey<T>, 
            |       value: T
            |    )
            |} 
        """.trimMargin()
}

internal fun applyResourceHacks(source: Source) {
    source.types().forEach {
        val className = it[NAME]
        it.optFlatMap(CONSTANTS)
            .filter { it[TYPE] == JS_STRING }
            .filter { it[NAME].endsWith("_KEY") }
            .forEach { it[TYPE] = getType(className, it[NAME]) }
    }

    source.type("CanvasComponent")
        .property("resources")[TYPE] = RESOURCE_MAP

    val DEFAULT_PORT_CANDIDATE_DESCRIPTOR = "DefaultPortCandidateDescriptor"
    source.type(DEFAULT_PORT_CANDIDATE_DESCRIPTOR)
        .method("setTemplate").apply {
            val typeParameter = getVisualTemplateParameter(DEFAULT_PORT_CANDIDATE_DESCRIPTOR)
            firstParameter[TYPE] = resourceKey("$IVISUAL_TEMPLATE<$typeParameter>")
            secondParameter.addGeneric(typeParameter)
        }


    val RECTANGLE_INDICATOR_INSTALLER = "RectangleIndicatorInstaller"
    source.type(RECTANGLE_INDICATOR_INSTALLER)
        .flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "resourceKey" }
        .single { it[TYPE] == JS_STRING }
        .also { it[TYPE] = resourceKey("$IVISUAL_TEMPLATE<${getVisualTemplateParameter(RECTANGLE_INDICATOR_INSTALLER)}>") }

    val ORIENTED_RECTANGLE_INDICATOR_INSTALLER = "OrientedRectangleIndicatorInstaller"
    source.type(ORIENTED_RECTANGLE_INDICATOR_INSTALLER)
        .flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "templateKey" }
        .single { it[TYPE] == JS_STRING }
        .also { it[TYPE] = resourceKey("$IVISUAL_TEMPLATE<${getVisualTemplateParameter(ORIENTED_RECTANGLE_INDICATOR_INSTALLER)}>") }
}

private fun getType(
    className: String,
    name: String
): String {
    val typeParameter = when {
        name == "TEMPLATE_KEY" -> JS_STRING
        name.endsWith("_FILL_KEY") -> "yfiles.view.Fill"
        name.endsWith("STROKE_KEY") -> "yfiles.view.Stroke"
        else -> "$IVISUAL_TEMPLATE<${getVisualTemplateParameter(className)}>"
    }

    return resourceKey(typeParameter)
}
