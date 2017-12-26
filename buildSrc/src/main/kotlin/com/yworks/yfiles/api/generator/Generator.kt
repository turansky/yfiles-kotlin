@file:JvmName("Generator")

package com.yworks.yfiles.api.generator

import org.json.JSONObject
import java.io.File
import java.net.URL

private fun loadApiJson(path: String): String {
    return URL(path)
            .readText(DEFAULT_CHARSET)
            .removePrefix("var apiData=")
}

fun generateKotlinWrappers(apiPath: String, sourceDir: File) {
    val source = JSONObject(loadApiJson(apiPath))

    val apiRoot = ApiRoot(source)
    val types = apiRoot
            .namespaces.first { it.id == "yfiles" }
            .namespaces.flatMap { it.types }
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistryImpl(types)

    val fileGenerator = FileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)

    listOf("Boolean", "Number", "String", "Struct")
            .forEach { baseType -> sourceDir.resolve("yfiles/lang/${baseType}.kt").delete() }
}