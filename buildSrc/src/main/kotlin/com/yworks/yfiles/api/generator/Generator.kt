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
    "yfiles.lang.Struct",
    "yfiles.lang.Enum",
    "yfiles.lang.EnumDefinition"
).map { fixPackage(it) }

private fun loadApiJson(path: String): String {
    return URL(path)
        .readText(DEFAULT_CHARSET)
        .removePrefix("var apiData=")
}

fun generateKotlinWrappers(apiPath: String, sourceDir: File) {
    TypeParser.standardTypeMap = KotlinTypes.STANDARD_TYPE_MAP
    generateWrappers(apiPath, sourceDir, ::KotlinFileGenerator)
}

fun generateJavaWrappers(apiPath: String, sourceDir: File) {
    TypeParser.standardTypeMap = JavaTypes.STANDARD_TYPE_MAP
    generateWrappers(apiPath, sourceDir, ::JavaFileGenerator)
}

private fun generateWrappers(
    apiPath: String,
    sourceDir: File,
    createFileGenerator: (types: Iterable<Type>, functionSignatures: Iterable<FunctionSignature>) -> FileGenerator
) {
    val source = JSONObject(loadApiJson(apiPath))

    Hacks.applyHacks(source)

    val apiRoot = ApiRoot(source)
    val types = apiRoot
        .namespaces.first { it.id == YFILES_NAMESPACE }
        .namespaces.flatMap { it.types }
        .filterNot { PRIMITIVE_TYPES.contains(it.fqn) }

    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistryImpl(types)

    val fileGenerator = createFileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)
}