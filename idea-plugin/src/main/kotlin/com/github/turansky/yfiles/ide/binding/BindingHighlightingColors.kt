package com.github.turansky.yfiles.ide.binding

import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import org.jetbrains.kotlin.idea.highlighter.KotlinHighlightingColors

object BindingHighlightingColors {
    val LANGUAGE_INJECTION = createTextAttributesKey("BINDING_LANGUAGE_INJECTION", XmlHighlighterColors.XML_INJECTED_LANGUAGE_FRAGMENT)
    val BRACE = createTextAttributesKey("BINDING_BRACE", XmlHighlighterColors.XML_ATTRIBUTE_NAME)
    val KEYWORD = createTextAttributesKey("BINDING_KEYWORD", XmlHighlighterColors.XML_NS_PREFIX)

    val NAMED_ARGUMENT = createTextAttributesKey("BINDING_NAMED_ARGUMENT", KotlinHighlightingColors.NAMED_ARGUMENT)
    val ASSIGN = createTextAttributesKey("BINDING_ASSIGN", NAMED_ARGUMENT)

    val ARGUMENT = createTextAttributesKey("BINDING_ARGUMENT", KotlinHighlightingColors.STRING_ESCAPE)
    val COMMA = createTextAttributesKey("BINDING_COMMA", ARGUMENT)
}
