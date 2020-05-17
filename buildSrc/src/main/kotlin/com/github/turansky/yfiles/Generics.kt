package com.github.turansky.yfiles

internal interface ITypeParameter {
    val name: String

    fun toCode(): String
}

internal fun Generics(parameters: List<ITypeParameter>): Generics =
    if (parameters.isNotEmpty()) {
        SimpleGenerics(parameters)
    } else {
        EmptyGenerics
    }

internal sealed class Generics {
    protected abstract val parameters: List<ITypeParameter>

    abstract val declaration: String

    abstract fun asParameters(): String
    abstract fun asAliasParameters(): String

    abstract val placeholder: String

    abstract fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean = !isEmpty()

    operator fun plus(other: Generics): Generics =
        Generics(parameters + other.parameters)
}

private object EmptyGenerics : Generics() {
    override val parameters: List<ITypeParameter> = emptyList()

    override val declaration: String = ""
    override fun asParameters(): String = ""
    override fun asAliasParameters(): String = ""
    override val placeholder: String = ""

    override fun isEmpty(): Boolean = true
}

private class SimpleGenerics(override val parameters: List<ITypeParameter>) : Generics() {
    override val declaration: String
        get() = "<${parameters.byComma { it.toCode() }}> "

    override fun asParameters(): String =
        asParameters { it }

    override fun asAliasParameters(): String =
        asParameters { it.removePrefix("in ").removePrefix("out ") }

    private fun asParameters(transform: (String) -> String): String =
        "<${parameters.byComma { transform(it.name) }}> "

    override val placeholder: String
        get() = "<" + (1..parameters.size).map { "*" }.joinToString(",") + ">"

    override fun isEmpty(): Boolean = false
}
