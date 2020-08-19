package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*

private const val ITEM_TAG: String = "yfiles.graph.IModelItem.tag"
private const val CONTEXT: String = "yfiles.styles.ITemplateStyleBindingContext"

internal sealed class Binding {
    abstract val parentName: String
    abstract val name: String?
    abstract val converter: String?
    abstract val parameter: String?

    abstract val reference: String

    fun toCode(): String {
        val target = join(parentName, ".", name)
        val converter = converter ?: return target
        val parameter = parameter ?: return "$converter($target)"
        return "$converter($target, '$parameter')"
    }
}

private data class TagBinding(
    override val name: String?,
    override val converter: String?,
    override val parameter: String?
) : Binding() {
    override val parentName: String = "tag"
    override val reference: String = name ?: ITEM_TAG
}

private data class TemplateBinding(
    override val name: String?,
    override val converter: String?,
    override val parameter: String?
) : Binding() {
    override val parentName: String = "context"
    override val reference: String = join(CONTEXT, ".", name)
}

internal fun String.toBinding(): Binding? {
    val code = trimBraces() ?: return null
    val blocks = code.split(",")
    if (blocks.size !in 1..3) return null

    val dataMap = mutableMapOf<BindingDirective, String?>()
    for (block in blocks) {
        val data = block.trim().split(" ", "=")
        if (data.size > 2) return null

        val directive = BindingDirective.find(data[0]) ?: return null
        if (dataMap.containsKey(directive)) return null

        dataMap[directive] = if (data.size > 1) data[1] else null
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
