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

internal enum class PropertyMode(
    val readable: Boolean,
    val writable: Boolean
) {
    READ_WRITE(true, true),
    READ_ONLY(true, false),
    WRITE_ONLY(false, true)
}

internal class ParameterModifiers(flags: List<String>) {
    val vararg = VARARGS in flags
    val optional = OPTIONAL in flags

    private val canbenull = CANBENULL in flags
    val nullability = exp(canbenull, "?")
}

internal class EventListenerModifiers(flags: List<String>) {
    val public = PUBLIC in flags
    val abstract = ABSTRACT in flags
}

internal class Modifiers(modifiers: List<String>) {
    val flags = FLAGS in modifiers
    val static = STATIC in modifiers
    val final = FINAL in modifiers

    val mode = when {
        RO in modifiers -> READ_ONLY
        WO in modifiers -> WRITE_ONLY
        else -> READ_WRITE
    }

    val abstract = ABSTRACT in modifiers
    val internal = INTERNAL in modifiers
    val protected = PROTECTED in modifiers

    private val canbenull = CANBENULL in modifiers
    val nullability = exp(canbenull, "?")

    val hidden = HIDDEN in modifiers
}
