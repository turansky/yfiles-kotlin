package com.yworks.yfiles.api.generator

import java.io.File

internal interface FileGenerator {
    fun generate(directory: File)
}

internal class FQN(private val fqn: String) {
    private val names = fqn.split(".")
    private val packageNames = names.subList(0, names.size - 1)

    val name = names.last()
    val packageName = packageNames.joinToString(separator = ".")
    val path = packageNames.joinToString(separator = "/")

    override fun equals(other: Any?): Boolean {
        return other is FQN && other.fqn == fqn
    }

    override fun hashCode(): Int {
        return fqn.hashCode()
    }
}