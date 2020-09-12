package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*
import com.github.turansky.yfiles.ide.binding.BindingToken.*

internal object BindingParser {
    private val KEYWORD_REGEX = Regex("\\s*($BINDING|$TEMPLATE_BINDING)\\s*(\\S*)\\s*")
    private val ARGUMENT_REGEX = Regex("\\s*($CONVERTER|$PARAMETER)\\s*(=)\\s*(\\S*)\\s*")

    fun find(source: String): Pair<BindingDirective, String?>? {
        source.find(KEYWORD_REGEX)?.also { result ->
            val (directive, value) = result.destructured
            return BindingDirective.find(directive) to value.ifEmpty { null }
        }

        source.find(ARGUMENT_REGEX)?.also { result ->
            val (directive, _, value) = result.destructured
            return BindingDirective.find(directive) to value.ifEmpty { null }
        }

        return null
    }

    fun parse(source: String): List<BindingParseResult> {
        source.find(KEYWORD_REGEX)?.also { result ->
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

        source.find(ARGUMENT_REGEX)?.also { result ->
            val argumentMode = result.directive == CONVERTER

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

    private fun String.find(exp: Regex): MatchResult? =
        if (exp.matches(this)) {
            exp.find(this)
        } else {
            null
        }
}

internal data class BindingParseResult(
    val token: BindingToken,
    val range: IntRange
)

private val MatchResult.directive: BindingDirective
    get() = BindingDirective.find(groups[1]!!.value)

private fun MatchResult.r(index: Int): IntRange =
    groups[index]!!.range
