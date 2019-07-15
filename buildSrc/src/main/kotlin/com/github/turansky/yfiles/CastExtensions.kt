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
        |inline fun $generics Any?.is$className():Boolean {
        |   return this is $classDeclaration
        |}
        |
        |inline fun $generics Any?.as$className(): $classDeclaration? {
        |   return this as? $classDeclaration
        |}
        |
        |inline fun $generics Any?.to$className(): $classDeclaration {
        |   return this as $classDeclaration
        |}
    """.trimMargin()
}

fun interfaceCastExtensions(
    className: String,
    generics: String
): String {
    val yclass = "${className}.yclass"
    val classDeclaration = className + generics

    return """
        |inline fun Any?.is$className():Boolean {
        |   return ${yclass}.isInstance(this)
        |}
        |
        |inline fun $generics Any?.as$className(): $classDeclaration? {
        |   return if ( is$className() ) {
        |       unsafeCast<$classDeclaration>()
        |   } else {
        |       null
        |   }
        |}
        |
        |inline fun $generics Any?.to$className(): $classDeclaration {
        |   return as$className()!!
        |}
    """.trimMargin()
}