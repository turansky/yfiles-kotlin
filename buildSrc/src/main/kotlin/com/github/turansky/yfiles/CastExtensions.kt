package com.github.turansky.yfiles

internal fun interfaceCastExtensions(
    className: String,
    generics: Generics
): String {
    val yclass = "${className}.yclass"
    val classDeclaration = className + generics.asParameters()

    return """
        |inline fun Any?.is$className():Boolean {
        |   return ${yclass}.isInstance(this)
        |}
        |
        |inline fun ${generics.declaration} Any?.opt$className(): $classDeclaration? {
        |   return if (is$className()) {
        |       unsafeCast<$classDeclaration>()
        |   } else {
        |       null
        |   }
        |}
        |
        |inline fun ${generics.declaration} Any?.as$className(): $classDeclaration {
        |   require(is$className())
        |   return unsafeCast<$classDeclaration>()
        |}
    """.trimMargin()
}