package com.github.turansky.yfiles

fun classCastExtensions(
    className: String,
    generics: String
): String {
    val yclass = "${className}.yclass"
    val classDeclaration = className + generics

    return """
        |inline fun Any?.is$className() = 
        |   ${yclass}.isInstance(this)
        |
        |inline fun $generics Any?.as$className(): $classDeclaration? =
        |   if ( is$className() ) {
        |       unsafeCast<$classDeclaration>()
        |   } else {
        |       null
        |   }
        |
        |inline fun $generics Any?.to$className(): $classDeclaration =
        |   as$className()!!
    """.trimMargin()
}