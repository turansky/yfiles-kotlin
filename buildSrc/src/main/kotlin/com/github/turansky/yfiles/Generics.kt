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
    abstract val wrapperDeclaration: String

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
    override val wrapperDeclaration: String = ""
    override fun asParameters(): String = ""
    override fun asAliasParameters(): String = ""
    override val placeholder: String = ""

    override fun isEmpty(): Boolean = true
}

private class SimpleGenerics(override val parameters: List<ITypeParameter>) : Generics() {
    override val declaration: String
        get() = toString { it.toCode() }

    override val wrapperDeclaration: String
        get() = toString { it.toCode().clearName() }

    override fun asParameters(): String =
        toString { it.name }

    override fun asAliasParameters(): String =
        toString { it.name.clearName() }

    override val placeholder: String
        get() = toString { "*" }

    override fun isEmpty(): Boolean = false

    private fun toString(transform: (ITypeParameter) -> String): String =
        "<${parameters.byComma { transform(it) }}>"
}

private fun String.clearName(): String =
    removePrefix("in ").removePrefix("out ")

