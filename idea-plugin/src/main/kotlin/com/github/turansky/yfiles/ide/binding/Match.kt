package com.github.turansky.yfiles.ide.binding

internal fun MatchResult.g(index: Int): MatchGroup =
    groups[index]!!

internal fun MatchResult.r(index: Int): IntRange =
    g(index).range

internal val MatchGroup.valueAsDirective: BindingDirective
    get() = BindingDirective.find(value)!!
