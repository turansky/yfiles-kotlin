package com.github.turansky.yfiles

import com.github.turansky.yfiles.Nullability.NONNULL
import com.github.turansky.yfiles.Nullability.NULLABLE

internal val PUBLIC = "public"

internal val STATIC = "static"
internal val FINAL = "final"
internal val RO = "ro"
internal val ABSTRACT = "abstract"
internal val PROTECTED = "protected"

internal val ARTIFICIAL = "artificial"
internal val VARARGS = "varargs"
internal val OPTIONAL = "optional"
internal val CONVERSION = "conversion"

internal val CANBENULL = "canbenull"

enum class Nullability {
    NULLABLE,
    NONNULL
}

fun nullability(canbenull: Boolean): Nullability =
    if (canbenull) {
        NULLABLE
    } else {
        NONNULL
    }
