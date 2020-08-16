package com.github.turansky.yfiles.ide.documentation

import com.intellij.codeInsight.documentation.DocumentationManager.createHyperlink
import com.intellij.lang.documentation.DocumentationMarkup.*
import org.jetbrains.kotlin.idea.KotlinBundle

private const val SVG_TEMPLATES_URL: String = "https://docs.yworks.com/yfileshtml/#/dguide/custom-styles_template-styles"
private const val TEMPLATE_BINDING_URL: String = "https://docs.yworks.com/yfileshtml/%23/dguide/custom-styles_template-styles#_template_binding"

internal fun documentation(toCode: () -> String): String =
    StringBuilder().apply {
        addReturnsBlock(toCode())
        addSeeAlsoBlock()
    }.toString()

private fun StringBuilder.addReturnsBlock(code: String) {
    renderSection(KotlinBundle.message("kdoc.section.title.returns")) {
        append(code)
    }
}

private fun StringBuilder.addSeeAlsoBlock() {
    renderSection(KotlinBundle.message("kdoc.section.title.see.also")) {
        createHyperlink(this, SVG_TEMPLATES_URL, "SVG Templates in Styles", false)
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
