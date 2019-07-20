package com.github.turansky.yfiles

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
        |inline fun $generics Any?.opt$className(): $classDeclaration? {
        |   return if ( is$className() ) {
        |       unsafeCast<$classDeclaration>()
        |   } else {
        |       null
        |   }
        |}
        |
        |inline fun $generics Any?.to$className(): $classDeclaration {
        |   return opt$className()!!
        |}
    """.trimMargin()
}