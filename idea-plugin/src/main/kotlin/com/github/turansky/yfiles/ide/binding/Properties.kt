package com.github.turansky.yfiles.ide.binding

internal interface IProperty {
    val name: String
    val className: String

    val isStandard: Boolean
}
