@file:JvmName("Generator")

package com.yworks.yfiles.api.generator

import org.json.JSONObject
import java.io.File
import java.net.URL

private val YFILES_NAMESPACE = "yfiles"

private val PRIMITIVE_TYPES = listOf(
        "yfiles.lang.Boolean",
        "yfiles.lang.Number",
        "yfiles.lang.String",
        "yfiles.lang.Struct"
)

private fun loadApiJson(path: String): String {
    return URL(path)
            .readText(DEFAULT_CHARSET)
            .removePrefix("var apiData=")
}

fun generateKotlinWrappers(apiPath: String, sourceDir: File) {
    val source = JSONObject(loadApiJson(apiPath))

    Hacks.addComparisonClass(source)

    val apiRoot = ApiRoot(source)
    val types = apiRoot
            .namespaces.first { it.id == YFILES_NAMESPACE }
            .namespaces.flatMap { it.types }
            .filterNot { PRIMITIVE_TYPES.contains(it.fqn) }

    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistryImpl(types)

    val fileGenerator = FileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)
}