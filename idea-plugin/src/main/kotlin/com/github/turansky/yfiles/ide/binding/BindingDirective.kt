package com.github.turansky.yfiles.ide.binding

internal enum class BindingDirective(
    val key: String,
) {
    BINDING("Binding"),
    TEMPLATE_BINDING("TemplateBinding"),
    CONVERTER("Converter"),
    PARAMETER("Parameter");

    override fun toString(): String = key

    companion object {
        private val map = values().associateBy { it.key }

        fun find(key: String): BindingDirective = map.getValue(key)
    }
}
