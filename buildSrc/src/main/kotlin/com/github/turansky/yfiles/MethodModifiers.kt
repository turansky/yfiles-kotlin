package com.github.turansky.yfiles

import com.github.turansky.yfiles.PropertyMode.*

internal const val PUBLIC = "public"
internal const val FLAGS = "flags"

internal const val STATIC = "static"
internal const val FINAL = "final"
internal const val RO = "ro"
internal const val WO = "wo"
internal const val ABSTRACT = "abstract"
internal const val INTERNAL = "internal"
internal const val PROTECTED = "protected"

internal const val ARTIFICIAL = "artificial"
internal const val VARARGS = "varargs"
internal const val OPTIONAL = "optional"
internal const val CONVERSION = "conversion"

internal const val CANBENULL = "canbenull"

// for codegen
internal const val HIDDEN = "hidden"

internal const val EXPERT = "expert"

sealed class Modifiers(
    private val modifiers: List<String>,
    validModifiers: Set<String>? = null
) {
    init {
        if (validModifiers != null) {
            check(validModifiers.containsAll(modifiers)) {
                "Invalid modifiers: ${modifiers - validModifiers}"
            }
        }
    }

    protected fun has(modifier: String): Boolean =
        modifier in modifiers
}

internal enum class ClassMode {
    FINAL,
    OPEN,
    ABSTRACT
}

private val CLASS_MODIFIERS = setOf(
    ABSTRACT,
    FINAL,

    PUBLIC,
    EXPERT
)

internal class ClassModifiers(modifiers: List<String>) : Modifiers(modifiers, CLASS_MODIFIERS) {
    val mode: ClassMode = when {
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

internal enum class PropertyMode(
    val readable: Boolean,
    val writable: Boolean
) {
    READ_WRITE(true, true),
    READ_ONLY(true, false),
    WRITE_ONLY(false, true)
}

internal class PropertyModifiers(modifiers: List<String>) : Modifiers(modifiers) {
    val static = has(STATIC)
    val final = has(FINAL)

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

internal class MethodModifiers(modifiers: List<String>) : Modifiers(modifiers) {
    val static = has(STATIC)
    val final = has(FINAL)

    val abstract = has(ABSTRACT)
    val protected = has(PROTECTED)

    private val canbenull = has(CANBENULL)
    val nullability = exp(canbenull, "?")

    val hidden = has(HIDDEN)
}

internal class ParameterModifiers(modifiers: List<String>) : Modifiers(modifiers) {
    val vararg = has(VARARGS)
    val optional = has(OPTIONAL)

    private val canbenull = has(CANBENULL)
    val nullability = exp(canbenull, "?")
}

internal class EventListenerModifiers(modifiers: List<String>) : Modifiers(modifiers) {
    val public = has(PUBLIC)
    val abstract = has(ABSTRACT)
}
