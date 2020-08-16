package com.github.turansky.yfiles.ide.documentation

import com.intellij.codeInsight.documentation.DocumentationManager.createHyperlink
import com.intellij.lang.documentation.DocumentationMarkup.*
import org.jetbrains.kotlin.idea.KotlinBundle

private const val SVG_TEMPLATES_URL: String = "https://docs.yworks.com/yfileshtml/#/dguide/custom-styles_template-styles"
private const val TEMPLATE_BINDING_URL: String = "https://docs.yworks.com/yfileshtml/%23/dguide/custom-styles_template-styles#_template_binding"

internal fun documentation(binding: Binding): String =
    StringBuilder().apply {
        renderBindingBlock(binding)
        renderConverterBlock(binding)

        renderReturnsBlock(binding.toCode())
        renderSeeAlsoBlock()
    }.toString()

private fun StringBuilder.renderBindingBlock(binding: Binding) {
    renderSection("Binding") {
        createHyperlink(this, binding.reference, binding.name ?: binding.parentName, false)
    }
}

private fun StringBuilder.renderConverterBlock(binding: Binding) {
    val converter = binding.converter ?: return

    renderSection("Converter") {
        createHyperlink(this, converter, converter, false)
    }
}

private fun StringBuilder.renderReturnsBlock(code: String) {
    renderSection(KotlinBundle.message("kdoc.section.title.returns")) {
        append("<pre><code>")
        append(code)
        append("</code></pre>")
    }
}

private fun StringBuilder.renderSeeAlsoBlock() {
    renderSection(KotlinBundle.message("kdoc.section.title.see.also")) {
        createHyperlink(this, SVG_TEMPLATES_URL, "SVG Templates", false)
        append(", ")
        createHyperlink(this, TEMPLATE_BINDING_URL, "Template Binding", false)
    }
}

private fun StringBuilder.renderSection(
    title: String, content:
    StringBuilder.() -> Unit
) {
    append(SECTION_HEADER_START, title, ":", SECTION_SEPARATOR)
    content()
    append(SECTION_END)
}
