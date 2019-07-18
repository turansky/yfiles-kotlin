package com.github.turansky.yfiles

import java.io.File

internal interface FileGenerator {
    fun generate(directory: File)
}

internal open class GeneratorData(
    fqn: String
) {
    private val names = fqn.split(".")
    private val packageNames = names.subList(0, names.size - 1)

    val name = names.last()
    val packageName = packageNames.joinToString(separator = ".")
    val path = packageNames.joinToString(separator = "/")
}

internal data class TypeGeneratorData(
    private val fqn: String,
    private val alias: String?
) : GeneratorData(fqn) {
    val jsName = alias ?: name

    val primitive: Boolean
        get() = isPrimitiveClass(fqn)

    val marker: Boolean
        get() = isMarkerClass(fqn)
}

internal fun es6GeneratorData(
    declaration: Type
) = TypeGeneratorData(
    fqn = declaration.classId,
    alias = declaration.es6name
)