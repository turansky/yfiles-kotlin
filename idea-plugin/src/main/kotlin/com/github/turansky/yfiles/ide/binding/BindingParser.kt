package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*
import com.github.turansky.yfiles.ide.binding.BindingToken.*

internal object BindingParser {
    private val KEYWORD_REGEX = Regex("\\s*($BINDING|$TEMPLATE_BINDING)\\s*(\\S*)\\s*")
    private val ARGUMENT_REGEX = Regex("\\s*($CONVERTER|$PARAMETER)\\s*(=)\\s*(\\S*)\\s*")

    fun find(source: String): Pair<BindingDirective, String?>? =
        when {
            KEYWORD_REGEX.matches(source) -> {
                val (directive, value) = KEYWORD_REGEX.find(source)!!.destructured
                BindingDirective.find(directive) to value.ifEmpty { null }
            }

            ARGUMENT_REGEX.matches(source) -> {
                val (directive, _, value) = ARGUMENT_REGEX.find(source)!!.destructured
                BindingDirective.find(directive) to value.ifEmpty { null }
            }

            else -> null
        }

    fun parse(source: String): List<BindingParseResult> {
        KEYWORD_REGEX.find(source)?.also { result ->
            val keywordRange = result.r(1)
            val argumentRange = result.r(2)

            return if (argumentRange.isEmpty()) {
                listOf(
                    BindingParseResult(KEYWORD, keywordRange)
                )
            } else {
                listOf(
                    BindingParseResult(KEYWORD, keywordRange),
                    BindingParseResult(ARGUMENT, argumentRange)
                )
            }
        }

        ARGUMENT_REGEX.find(source)?.also { result ->
            val argumentMode = result.d(1) == CONVERTER

            return listOf(
                BindingParseResult(KEYWORD, result.r(1)),
                BindingParseResult(ASSIGN, result.r(2)),
                BindingParseResult(if (argumentMode) ARGUMENT else VALUE, result.r(3))
            )
        }

        return listOf(
            BindingParseResult(ERROR, source.indices)
        )
    }
}

internal data class BindingParseResult(
    val token: BindingToken,
    val range: IntRange
)
