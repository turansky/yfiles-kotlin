package com.github.turansky.yfiles.ide.binding

import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

internal object BindingHighlightingColors {
    private val LANGUAGE_INJECTION = createTextAttributesKey("BINDING_LANGUAGE_INJECTION", XmlHighlighterColors.XML_INJECTED_LANGUAGE_FRAGMENT)
    private val BRACE = createTextAttributesKey("BINDING_BRACE", XmlHighlighterColors.XML_ATTRIBUTE_NAME)
    private val KEYWORD = createTextAttributesKey("BINDING_KEYWORD", XmlHighlighterColors.XML_NS_PREFIX)
    private val ASSIGN = createTextAttributesKey("BINDING_ASSIGN", KEYWORD)

    private val ARGUMENT = createTextAttributesKey("BINDING_ARGUMENT", XmlHighlighterColors.XML_ATTRIBUTE_NAME)
    private val VALUE = createTextAttributesKey("BINDING_VALUE", XmlHighlighterColors.XML_ATTRIBUTE_VALUE)
    private val COMMA = createTextAttributesKey("BINDING_COMMA", ARGUMENT)

    private val ERROR = createTextAttributesKey("ERROR", CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES)

    private val MAP = mapOf(
        BindingToken.LANGUAGE_INJECTION to LANGUAGE_INJECTION,
        BindingToken.BRACE to BRACE,
        BindingToken.COMMA to COMMA,

        BindingToken.KEYWORD to KEYWORD,
        BindingToken.ASSIGN to ASSIGN,
        BindingToken.ARGUMENT to ARGUMENT,
        BindingToken.VALUE to VALUE,
        BindingToken.ERROR to ERROR,
    )

    operator fun get(token: BindingToken): TextAttributesKey =
        MAP.getValue(token)
}
