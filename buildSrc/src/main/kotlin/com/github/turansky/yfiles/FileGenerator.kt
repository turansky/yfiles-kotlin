package com.github.turansky.yfiles

internal interface FileGenerator {
    fun generate(context: GeneratorContext)
}

internal open class GeneratorData(
    val fqn: String,
) {
    private val names = fqn.split(".")
    private val packageNames = names.subList(0, names.size - 1)

    val name = names.last()
    val packageName = packageNames.joinToString(separator = ".")
}

internal class TypeGeneratorData(
    fqn: String,
    alias: String?,
) : GeneratorData(fqn) {
    val jsName = alias ?: name

    val fileId: String
        get() = if (primitive) {
            "$packageName.$jsName"
        } else {
            fqn
        }

    val primitive: Boolean
        get() = isPrimitiveClass(fqn)

    val marker: Boolean
        get() = isMarkerClass(fqn)
}

internal fun es6GeneratorData(
    declaration: Type,
) = TypeGeneratorData(
    fqn = declaration.classId,
    alias = declaration.es6name
)
