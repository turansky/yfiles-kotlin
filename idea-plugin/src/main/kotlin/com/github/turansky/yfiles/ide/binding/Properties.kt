package com.github.turansky.yfiles.ide.binding

internal object Properties {
    val CONVERTERS: IProperty = SimpleProperty("CONVERTERS", "yfiles.styles.Templates")
}

internal interface IProperty {
    val name: String
    val className: String

    val isStandard: Boolean
}

private class SimpleProperty(
    override val name: String,
    override val className: String
) : IProperty {
    override val isStandard: Boolean = true
}
