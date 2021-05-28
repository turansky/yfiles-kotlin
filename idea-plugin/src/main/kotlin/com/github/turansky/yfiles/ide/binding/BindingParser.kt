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
            BindingParseResult(LANGUAGE_INJECTION, TextRange.allOf(source)),
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
            val directive = result.directive
            val parseResults = listOf(
                BindingParseResult(KEYWORD, result.r(1), directive)
            )

            val argumentRange = result.r(2)
                .takeIf { !it.isEmpty }
                ?: return parseResults

            return parseResults + BindingParseResult(ARGUMENT, argumentRange, directive)
        }

        source.find(ARGUMENT_REGEX)?.also { result ->
            val directive = result.directive
            return listOf(
                BindingParseResult(KEYWORD, result.r(1), directive),
                BindingParseResult(ASSIGN, result.r(2)),
                BindingParseResult(if (directive == CONVERTER) ARGUMENT else VALUE, result.r(3), directive)
            )
        }

        return listOf(
            BindingParseResult(ERROR, TextRange.allOf(source))
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
    val range: TextRange,
    val directive: BindingDirective? = null,
)

private fun BindingParseResult(
    token: BindingToken,
    offset: Int,
): BindingParseResult =
    BindingParseResult(token, TextRange.from(offset, 1))

private val MatchResult.directive: BindingDirective
    get() = BindingDirective.find(groups[1]!!.value)

private fun MatchResult.r(index: Int): TextRange =
    groups[index]!!.range
        .let { TextRange(it.start, it.endInclusive + 1) }
