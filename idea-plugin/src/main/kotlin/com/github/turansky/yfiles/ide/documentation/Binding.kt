package com.github.turansky.yfiles.ide.documentation

private const val BINDING: String = "Binding"
private const val TEMPLATE_BINDING: String = "TemplateBinding"

private const val CONVERTER: String = "Converter"
private const val PARAMETER: String = "Parameter"

private val DIRECTIVES = setOf(
    BINDING,
    TEMPLATE_BINDING,

    CONVERTER,
    PARAMETER
)

internal sealed class Binding(private val parentName: String) {
    protected abstract val name: String?
    protected abstract val converter: String?
    protected abstract val parameter: String?

    private fun toCode(): String {
        val target = join(parentName, ".", name)
        val converter = converter ?: return target
        val parameters = join(target, ", ", parameter)
        return "$converter($parameters)"
    }

    fun toDoc(): String = documentation(toCode())
}

private data class TagBinding(
    override val name: String?,
    override val converter: String?,
    override val parameter: String?
) : Binding("tag")

private data class TemplateBinding(
    override val name: String?,
    override val converter: String?,
    override val parameter: String?
) : Binding("context")

internal fun String.toBinding(): Binding? {
    val code = trimBrackets() ?: return null
    val blocks = code.split(",")
    if (blocks.size !in 1..3) return null

    val dataMap = mutableMapOf<String, String?>()
    for (block in blocks) {
        val data = block.trim().split(" ")
        if (data.size > 2) return null

        val directive = data[0]
        if (directive !in DIRECTIVES || dataMap.containsKey(directive)) return null

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

private fun String.trimBrackets(): String? =
    when {
        !startsWith("{") -> null
        !endsWith("}") -> null
        else -> removePrefix("{").removeSuffix("}").trim().takeIf { it.isNotEmpty() }
    }

private fun join(
    first: String,
    delimiter: String,
    second: String?
): String =
    if (second != null) {
        "$first$delimiter$second"
    } else {
        first
    }
