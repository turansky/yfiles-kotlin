package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*

internal object BindingParser {
    val KEYWORD = Regex("\\s*($BINDING|$TEMPLATE_BINDING)\\s*(\\S*)\\s*")
    val ARGUMENT = Regex("\\s*($CONVERTER|$PARAMETER)\\s*(=)\\s*(\\S*)\\s*")

    fun find(source: String): Pair<BindingDirective, String?>? =
        when {
            KEYWORD.matches(source) -> {
                val (directive, value) = KEYWORD.find(source)!!.destructured
                BindingDirective.find(directive) to value.ifEmpty { null }
            }

            ARGUMENT.matches(source) -> {
                val (directive, _, value) = ARGUMENT.find(source)!!.destructured
                BindingDirective.find(directive) to value.ifEmpty { null }
            }

            else -> null
        }
}
