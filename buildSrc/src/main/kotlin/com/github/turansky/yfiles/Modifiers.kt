package com.github.turansky.yfiles

import com.github.turansky.yfiles.PropertyMode.*

internal const val PUBLIC = "public"
internal const val FLAGS = "flags"

internal const val STATIC = "static"
internal const val FINAL = "final"
internal const val VIRTUAL = "virtual"
internal const val RO = "ro"
internal const val WO = "wo"
internal const val SEALED = "sealed"
internal const val ABSTRACT = "abstract"
internal const val INTERNAL = "internal"
internal const val PROTECTED = "protected"

internal const val ARTIFICIAL = "artificial"
internal const val VARARGS = "varargs"
internal const val OPTIONAL = "optional"
internal const val CONVERSION = "conversion"

internal const val CANBENULL = "canbenull"
private const val NOTNULL = "notnull"

internal const val DEPRECATED = "deprecated"

// for codegen
internal const val HIDDEN = "hidden"

private const val EXPERT = "expert"

sealed class Modifiers(
    private val modifiers: List<String>,
    validModifiers: Set<String>
) {
    init {
        check(validModifiers.containsAll(modifiers)) {
            "Invalid modifiers: ${modifiers - validModifiers}"
        }
    }

    protected fun has(modifier: String): Boolean =
        modifier in modifiers
}

internal enum class ClassMode {
    FINAL,
    OPEN,
    SEALED,
    ABSTRACT
}

private val CLASS_MODIFIERS = setOf(
    SEALED,
    ABSTRACT,
    FINAL,

    PUBLIC,
    EXPERT
)

internal class ClassModifiers(modifiers: List<String>) : Modifiers(modifiers, CLASS_MODIFIERS) {
    val mode: ClassMode = when {
        has(SEALED) -> ClassMode.SEALED
        has(ABSTRACT) -> ClassMode.ABSTRACT
        has(FINAL) -> ClassMode.FINAL
        else -> ClassMode.OPEN
    }
}

private val ENUM_MODIFIERS = setOf(
    FLAGS,

    PUBLIC,
    EXPERT
)

internal class EnumModifiers(modifiers: List<String>) : Modifiers(modifiers, ENUM_MODIFIERS) {
    val flags = has(FLAGS)
}

internal enum class ConstructorVisibility {
    PUBLIC,
    PROTECTED,
    INTERNAL
}

private val CONSTRUCTOR_MODIFIERS = setOf(
    INTERNAL,
    PROTECTED,

    PUBLIC
)

internal class ConstructorModifiers(modifiers: List<String>) : Modifiers(modifiers, CONSTRUCTOR_MODIFIERS) {
    val visibility: ConstructorVisibility = when {
        has(INTERNAL) -> ConstructorVisibility.INTERNAL
        has(PROTECTED) -> ConstructorVisibility.PROTECTED
        else -> ConstructorVisibility.PUBLIC
    }
}

private val CONSTANT_MODIFIERS = setOf(
    STATIC,
    FINAL,
    RO,

    PROTECTED,

    PUBLIC,
    EXPERT,
    NOTNULL
)

internal class ConstantModifiers(modifiers: List<String>) : Modifiers(modifiers, CONSTANT_MODIFIERS) {
    val protected = has(PROTECTED)
}

internal enum class PropertyMode(
    val readable: Boolean,
    val writable: Boolean
) {
    READ_WRITE(true, true),
    READ_ONLY(true, false),
    WRITE_ONLY(false, true)
}

private val PROPERTY_MODIFIERS = setOf(
    STATIC,
    FINAL,

    RO, WO,

    ABSTRACT,
    PROTECTED,

    CANBENULL,

    DEPRECATED,

    PUBLIC,
    EXPERT,
    NOTNULL,
    CONVERSION,
    VIRTUAL
)

internal class PropertyModifiers(modifiers: List<String>) : Modifiers(modifiers, PROPERTY_MODIFIERS) {
    val static = has(STATIC)
    val final = has(FINAL)
    val deprecated = has(DEPRECATED)

    val mode = when {
        has(RO) -> READ_ONLY
        has(WO) -> WRITE_ONLY
        else -> READ_WRITE
    }

    val abstract = has(ABSTRACT)
    val protected = has(PROTECTED)

    private val canbenull = has(CANBENULL)
    val nullability = exp(canbenull, "?")
}

private val METHOD_MODIFIERS = setOf(
    STATIC,
    FINAL,

    ABSTRACT,
    PROTECTED,

    CANBENULL,

    DEPRECATED,

    HIDDEN,

    PUBLIC,
    EXPERT,
    NOTNULL,
    VIRTUAL
)

internal class MethodModifiers(modifiers: List<String>) : Modifiers(modifiers, METHOD_MODIFIERS) {
    val static = has(STATIC)
    val final = has(FINAL)
    val deprecated = has(DEPRECATED)

    val abstract = has(ABSTRACT)
    val protected = has(PROTECTED)

    private val canbenull = has(CANBENULL)
    val nullability = exp(canbenull, "?")

    val hidden = has(HIDDEN)
}

private val PARAMETER_MODIFIERS = setOf(
    VARARGS,
    OPTIONAL,

    CANBENULL,

    NOTNULL,
    CONVERSION
)

internal class ParameterModifiers(modifiers: List<String>) : Modifiers(modifiers, PARAMETER_MODIFIERS) {
    val vararg = has(VARARGS)
    val optional = has(OPTIONAL)

    private val canbenull = has(CANBENULL)
    val nullability = exp(canbenull, "?")
}

private val EVENT_LISTENERS_MODIFIERS = setOf(
    ABSTRACT,

    PUBLIC
)

internal class EventListenerModifiers(modifiers: List<String>) : Modifiers(modifiers, EVENT_LISTENERS_MODIFIERS) {
    val abstract = has(ABSTRACT)
}
