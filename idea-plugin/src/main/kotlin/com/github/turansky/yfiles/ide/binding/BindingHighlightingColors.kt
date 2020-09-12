package com.github.turansky.yfiles.ide.binding

import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object BindingHighlightingColors {
    val LANGUAGE_INJECTION = createTextAttributesKey("BINDING_LANGUAGE_INJECTION", XmlHighlighterColors.XML_INJECTED_LANGUAGE_FRAGMENT)
    val BRACE = createTextAttributesKey("BINDING_BRACE", XmlHighlighterColors.XML_ATTRIBUTE_NAME)
    val KEYWORD = createTextAttributesKey("BINDING_KEYWORD", XmlHighlighterColors.XML_NS_PREFIX)
    val ASSIGN = createTextAttributesKey("BINDING_ASSIGN", KEYWORD)

    val ARGUMENT = createTextAttributesKey("BINDING_ARGUMENT", XmlHighlighterColors.XML_ATTRIBUTE_NAME)
    val VALUE = createTextAttributesKey("BINDING_VALUE", XmlHighlighterColors.XML_ATTRIBUTE_VALUE)
    val COMMA = createTextAttributesKey("BINDING_COMMA", ARGUMENT)

    val ERROR = createTextAttributesKey("ERROR", CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES)
}
