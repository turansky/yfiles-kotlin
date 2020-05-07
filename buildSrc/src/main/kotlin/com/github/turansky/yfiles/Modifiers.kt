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

sealed class ModifiersBase(
    private val modifiers: List<String>
) {
    protected fun has(modifier: String): Boolean =
        modifier in modifiers
}

internal enum class ClassMode {
    FINAL,
    OPEN,
    ABSTRACT
}

internal class ClassModifiers(modifiers: List<String>) : ModifiersBase(modifiers) {
    val mode: ClassMode = when {
        has(ABSTRACT) -> ClassMode.ABSTRACT
        has(FINAL) -> ClassMode.FINAL
        else -> ClassMode.OPEN
    }
}

internal class EnumModifiers(modifiers: List<String>) : ModifiersBase(modifiers) {
    val flags = has(FLAGS)
}

internal enum class ConstructorVisibility {
    PUBLIC,
    PROTECTED,
    INTERNAL
}

internal class ConstructorModifiers(modifiers: List<String>) : ModifiersBase(modifiers) {
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

internal class PropertyModifiers(modifiers: List<String>) : ModifiersBase(modifiers) {
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

internal class ParameterModifiers(modifiers: List<String>) : ModifiersBase(modifiers) {
    val vararg = has(VARARGS)
    val optional = has(OPTIONAL)

    private val canbenull = has(CANBENULL)
    val nullability = exp(canbenull, "?")
}

internal class EventListenerModifiers(modifiers: List<String>) : ModifiersBase(modifiers) {
    val public = has(PUBLIC)
    val abstract = has(ABSTRACT)
}

internal class Modifiers(modifiers: List<String>) : ModifiersBase(modifiers) {
    val static = has(STATIC)
    val final = has(FINAL)

    val abstract = has(ABSTRACT)
    val internal = has(INTERNAL)
    val protected = has(PROTECTED)

    private val canbenull = has(CANBENULL)
    val nullability = exp(canbenull, "?")

    val hidden = has(HIDDEN)
}
