package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*

private const val TAG: String = "yfiles.graph.Tag"

internal sealed class Binding {
    abstract val parentName: String
    abstract val name: String?
    abstract val converter: String?
    abstract val parameter: String?

    abstract val parentReference: String

    fun toCode(): String {
        val target = join(parentName, ".", name)
        val converter = converter ?: return target
        val parameter = parameter ?: return "$converter($target)"
        return """$converter($target, "$parameter")"""
    }
}

internal data class TagBinding(
    override val name: String?,
    override val converter: String?,
    override val parameter: String?,
) : Binding() {
    override val parentName: String = "tag"
    override val parentReference: String = TAG
}

internal data class TemplateBinding(
    override val name: String?,
    override val converter: String?,
    override val parameter: String?,
) : Binding() {
    override val parentName: String = "context"
    override val parentReference: String = ContextProperty.findParentClass(name)
}

internal fun String.toBinding(): Binding? {
    val code = trimBraces() ?: return null
    val blocks = code.split(",")
    if (blocks.size !in 1..3) return null

    val dataMap = mutableMapOf<BindingDirective, String?>()
    for (block in blocks) {
        val (directive, value) = BindingParser.find(block) ?: continue

        if (dataMap.containsKey(directive)) return null

        dataMap[directive] = value
    }

    return when {
        dataMap.containsKey(BINDING)
        -> TagBinding(
            name = dataMap[BINDING],
            converter = dataMap[CONVERTER],
            parameter = dataMap[PARAMETER]
        )

        dataMap.containsKey(TEMPLATE_BINDING)
        -> TemplateBinding(
            name = dataMap[TEMPLATE_BINDING],
            converter = dataMap[CONVERTER],
            parameter = dataMap[PARAMETER]
        )

        else -> null
    }
}
