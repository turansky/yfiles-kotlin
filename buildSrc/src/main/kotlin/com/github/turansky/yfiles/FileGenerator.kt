package com.github.turansky.yfiles

import java.io.File

internal interface FileGenerator {
    fun generate(directory: File)
}

internal abstract class AbstractGeneratorData(
    fqn: String
) {
    private val names = fqn.split(".")
    private val packageNames = names.subList(0, names.size - 1)

    val name = names.last()
    val packageName = packageNames.joinToString(separator = ".")
    val path = packageNames.joinToString(separator = "/")
}

internal data class GeneratorData(
    private val fqn: String
) : AbstractGeneratorData(fqn)