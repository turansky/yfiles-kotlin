package com.github.turansky.yfiles

private const val T = "T"

private val INVOKE_TARGETS = setOf(
    "GraphComponent",

    "BendDecorator",
    "EdgeDecorator",
    "GraphDecorator",
    "LabelDecorator",
    "NodeDecorator",
    "PortDecorator",
    "StripeDecorator",
    "StripeLabelDecorator",
    "TableDecorator",

    "Visual"
)

internal fun invokeExtension(
    className: String,
    generics: Generics,
    final: Boolean = false
): String? {
    if (className !in INVOKE_TARGETS) {
        return null
    }

    val type = className + generics.asParameters()

    lateinit var receiverType: String
    lateinit var typeGenerics: Generics

    if (final) {
        receiverType = type
        typeGenerics = generics
    } else {
        receiverType = T
        typeGenerics = Generics(listOf(CustomTypeParameter(T, type))) + generics
    }

    return """
        inline operator fun ${typeGenerics.declaration} $receiverType.invoke(
            block: $receiverType.() -> Unit
        ): $receiverType = apply(block)
    """.trimIndent()
}
