package com.github.turansky.yfiles

private val INVOKE_TARGETS = setOf(
    "BendDecorator",
    "EdgeDecorator",
    "GraphDecorator",
    "LabelDecorator",
    "NodeDecorator",
    "PortDecorator",
    // TODO: support
    //  "StripeDecorator",
    "StripeLabelDecorator",
    "TableDecorator"
)

fun invokeExtension(
    className: String
): String? {
    if (className !in INVOKE_TARGETS) {
        return null
    }

    return """
        |inline operator fun $className.invoke(block: $className.() -> Unit) {
        |   block(this)
        |}
    """.trimMargin()
}