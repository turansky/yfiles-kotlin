package com.github.turansky.yfiles.ide.binding

internal enum class BindingDirective(
    private val key: String
) {
    BINDING("Binding"),
    TEMPLATE_BINDING("TemplateBinding"),
    CONVERTER("Converter"),
    PARAMETER("Parameter");

    companion object {
        private val map = values().asSequence().associateBy { it.key }

        fun find(key: String): BindingDirective? = map[key]
    }
}
