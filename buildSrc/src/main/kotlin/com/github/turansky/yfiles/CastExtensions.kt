package com.github.turansky.yfiles

fun classCastExtensions(
    className: String,
    generics: String
): String {
    if (generics.isNotEmpty()) {
        return interfaceCastExtensions(className, generics)
    }

    val classDeclaration = className + generics

    return """
        |inline fun $generics Any?.is$className():Boolean = 
        |   this is $classDeclaration
        |
        |inline fun $generics Any?.as$className(): $classDeclaration? =
        |   this as? $classDeclaration
        |
        |inline fun $generics Any?.to$className(): $classDeclaration =
        |   this as $classDeclaration
    """.trimMargin()
}

fun interfaceCastExtensions(
    className: String,
    generics: String
): String {
    val yclass = "${className}.yclass"
    val classDeclaration = className + generics

    return """
        |inline fun Any?.is$className():Boolean = 
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