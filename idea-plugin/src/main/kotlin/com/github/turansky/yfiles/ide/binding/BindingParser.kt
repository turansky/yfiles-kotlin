package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.*
import com.github.turansky.yfiles.ide.binding.BindingToken.*
import com.intellij.openapi.util.TextRange

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
        val result = mutableListOf(
            BindingParseResult(LANGUAGE_INJECTION, source.indices),
            BindingParseResult(BRACE, 0),
            BindingParseResult(BRACE, source.lastIndex),
        )

        val codeStartOffset = 1

        val code = source.drop(1).dropLast(1)
        val blocks = code.split(',')

        var localOffset = 0
        for (block in blocks) {
            val offset = codeStartOffset + localOffset

            result.addAll(parseBlock(block).map { it.copy(range = it.range.shiftRight(offset)) })

            localOffset += block.length + 1
            if (localOffset < code.length) {
                result.add(BindingParseResult(COMMA, codeStartOffset + localOffset - 1))
            }
        }

        return result
    }

    private fun parseBlock(source: String): List<BindingParseResult> {
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
    val range: TextRange
)

private fun BindingParseResult(
    token: BindingToken,
    range: IntRange
): BindingParseResult =
    BindingParseResult(token, TextRange.from(range.start, range.count()))

private fun BindingParseResult(
    token: BindingToken,
    offset: Int
): BindingParseResult =
    BindingParseResult(token, IntRange(offset, offset))

private val MatchResult.directive: BindingDirective
    get() = BindingDirective.find(groups[1]!!.value)

private fun MatchResult.r(index: Int): IntRange =
    groups[index]!!.range
