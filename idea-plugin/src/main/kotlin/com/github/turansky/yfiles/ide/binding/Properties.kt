package com.github.turansky.yfiles.ide.binding

internal object Properties {
    val CONVERTERS: IProperty = SimpleProperty("yfiles.styles.Templates", "CONVERTERS")
}

internal interface IProperty {
    val className: String
    val name: String

    val isStandard: Boolean
}

private class SimpleProperty(
    override val className: String,
    override val name: String
) : IProperty {
    override val isStandard: Boolean = true
}
