package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*

internal object BindingParser {
    private val KEYWORD = Regex("\\s*($BINDING|$TEMPLATE_BINDING)\\s*(\\S*)\\s*")
    private val ARGUMENT = Regex("\\s*($CONVERTER|$PARAMETER)\\s*(=)\\s*(\\S*)\\s*")

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

    fun parse(source: String): List<BindingParseResult> {
        KEYWORD.find(source)?.also { result ->
            val keywordRange = result.r(1)
            val argumentRange = result.r(2)

            return if (argumentRange.isEmpty()) {
                listOf(
                    BindingParseResult(BindingToken.KEYWORD, keywordRange)
                )
            } else {
                listOf(
                    BindingParseResult(BindingToken.KEYWORD, keywordRange),
                    BindingParseResult(BindingToken.ARGUMENT, argumentRange)
                )
            }
        }

        ARGUMENT.find(source)?.also { result ->
            val argumentMode = result.d(1) == CONVERTER

            return listOf(
                BindingParseResult(BindingToken.KEYWORD, result.r(1)),
                BindingParseResult(BindingToken.ASSIGN, result.r(2)),
                BindingParseResult(if (argumentMode) BindingToken.ARGUMENT else BindingToken.VALUE, result.r(3))
            )
        }

        return listOf(
            BindingParseResult(BindingToken.ERROR, source.indices)
        )
    }
}

internal data class BindingParseResult(
    val token: BindingToken,
    val range: IntRange
)
