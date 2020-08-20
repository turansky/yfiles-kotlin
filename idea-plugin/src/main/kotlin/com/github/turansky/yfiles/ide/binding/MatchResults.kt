package com.github.turansky.yfiles.ide.binding

fun MatchResult.g(index: Int): MatchGroup =
    groups[index]!!

fun MatchResult.r(index: Int): IntRange =
    g(index).range
