@file:JvmName("Generator")

package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.ProgrammingLanguage.JAVA
import com.yworks.yfiles.api.generator.ProgrammingLanguage.KOTLIN
import org.json.JSONObject
import java.io.File
import java.net.URL

private val YFILES_NAMESPACE = "yfiles"

private val PRIMITIVE_TYPES = sequenceOf(
    "yfiles.lang.Boolean",
    "yfiles.lang.Number",
    "yfiles.lang.Object",
    "yfiles.lang.String",
    "yfiles.lang.Struct",
    "yfiles.lang.Enum",
    "yfiles.lang.EnumDefinition"
)
    .map(::fixPackage)
    .toSet()

private fun loadApiJson(path: String): String {
    return URL(path)
        .readText(DEFAULT_CHARSET)
        .removePrefix("var apiData=")
}

fun generateKotlinWrappers(apiPath: String, apiVersion: ApiVersion, sourceDir: File) {
    TypeParser.standardTypeMap = KotlinTypes.STANDARD_TYPE_MAP
    TypeParser.javaArrayMode = false
    generateWrappers(
        apiPath = apiPath,
        apiVersion = apiVersion,
        language = KOTLIN,
        sourceDir = sourceDir,
        createFileGenerator = ::KotlinFileGenerator
    )
}

fun generateJavaWrappers(apiPath: String, apiVersion: ApiVersion, sourceDir: File) {
    TypeParser.standardTypeMap = JavaTypes.STANDARD_TYPE_MAP
    TypeParser.javaArrayMode = true
    generateWrappers(
        apiPath = apiPath,
        apiVersion = apiVersion,
        language = JAVA,
        sourceDir = sourceDir,
        createFileGenerator = ::JavaFileGenerator
    )
}

private fun generateWrappers(
    apiPath: String,
    apiVersion: ApiVersion,
    language: ProgrammingLanguage,
    sourceDir: File,
    createFileGenerator: (types: Iterable<Type>, functionSignatures: Iterable<FunctionSignature>) -> FileGenerator
) {
    val source = JSONObject(loadApiJson(apiPath))

    Hacks.applyHacks(source, apiVersion)

    val apiRoot = ApiRoot(source)
    val types = apiRoot
        .namespaces
        .asSequence()
        .first { it.id == YFILES_NAMESPACE }
        .namespaces
        .flatMap { it.types }
        .filterNot { PRIMITIVE_TYPES.contains(it.fqn) }

    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistryImpl(types, language)

    val fileGenerator = createFileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)
}