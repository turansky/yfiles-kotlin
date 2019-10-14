package com.github.turansky.yfiles

internal fun interfaceMetadataExtensions(
    className: String,
    generics: Generics
): String {
    val classDeclaration = className + generics.placeholder

    return """
        |inline fun $className():yfiles.lang.ClassMetadata<$classDeclaration> =
        |    $className.Companion
    """.trimMargin()
}