package com.github.turansky.yfiles.ide.binding

internal fun MatchResult.r(index: Int): IntRange =
    groups[index]!!.range
