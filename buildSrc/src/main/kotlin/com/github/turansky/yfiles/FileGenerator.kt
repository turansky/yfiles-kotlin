package com.github.turansky.yfiles

import com.github.turansky.yfiles.YModule.Companion.findModule
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

internal data class TypeGeneratorData(
    private val fqn: String,
    val modulePath: String
) : AbstractGeneratorData(fqn)

internal fun umdGeneratorData(
    fqn: String,
    modules: List<IModule>
) = TypeGeneratorData(
    fqn = fqn,
    modulePath = findModule(fqn, modules).path
)