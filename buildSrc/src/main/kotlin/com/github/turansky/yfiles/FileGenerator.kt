package com.github.turansky.yfiles

import com.github.turansky.yfiles.YModule.Companion.findModule
import com.github.turansky.yfiles.YModule.Companion.getQualifier
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
    val modulePath: String,
    private val alias: String? = null,
    private val getQualifier: ((packageName: String) -> String?)? = null
) : AbstractGeneratorData(fqn) {
    val jsName = alias ?: name
    val qualifier = getQualifier?.invoke(packageName)

    val marker: Boolean
        get() = isMarkerClass(fqn)
}

internal fun umdGeneratorData(
    declaration: Type
) = TypeGeneratorData(
    fqn = declaration.fqn,
    modulePath = findModule(
        declaration.fqn,
        declaration.modules
    ).path,
    getQualifier = ::getQualifier
)

internal fun es6GeneratorData(
    declaration: Type
) = TypeGeneratorData(
    fqn = declaration.fqn,
    modulePath = "yfiles/${declaration.es6Module}",
    alias = declaration.es6name
)