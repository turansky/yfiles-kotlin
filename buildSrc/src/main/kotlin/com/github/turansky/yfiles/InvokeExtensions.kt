package com.github.turansky.yfiles

private val INVOKE_TARGETS = setOf(
    "BendDecorator",
    "EdgeDecorator",
    "GraphDecorator",
    "LabelDecorator",
    "NodeDecorator",
    "PortDecorator",
    "StripeDecorator",
    "StripeLabelDecorator",
    "TableDecorator"
)

internal fun invokeExtension(
    className: String,
    generics: Generics
): String? {
    if (className !in INVOKE_TARGETS) {
        return null
    }

    val type = className + generics.asParameters()
    return """
        |inline operator fun ${generics.declaration} $type.invoke(
        |    block: $type.() -> Unit
        |) {
        |   block(this)
        |}
    """.trimMargin()
}