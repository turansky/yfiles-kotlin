package com.github.turansky.yfiles.ide.binding

internal fun MatchResult.d(index: Int): BindingDirective =
    BindingDirective.find(groups[index]!!.value)

internal fun MatchResult.r(index: Int): IntRange =
    groups[index]!!.range
