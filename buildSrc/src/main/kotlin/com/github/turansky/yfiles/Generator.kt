package com.github.turansky.yfiles

import com.github.turansky.yfiles.Hacks.applyHacks
import org.json.JSONObject
import java.io.File
import java.net.URL

private val YFILES_NAMESPACE = "yfiles"

private val EXCLUDED_TYPES = sequenceOf(
    "yfiles.lang.Struct",

    "yfiles.lang.Enum",
    "yfiles.lang.EnumDefinition",

    "yfiles.lang.Interface",
    "yfiles.lang.InterfaceDefinition",

    "yfiles.lang.ClassDefinition",

    "yfiles.lang.delegate",
    "yfiles.lang.Exception",
    "yfiles.lang.Trait"
)
    .map(::fixPackage)
    .toSet()

private fun loadApiJson(path: String): String {
    return URL(path)
        .readText(DEFAULT_CHARSET)
        .removePrefix("var apiData=")
}

fun generateKotlinWrappers(apiPath: String, sourceDir: File) {
    TypeParser.standardTypeMap = KotlinTypes.STANDARD_TYPE_MAP
    generateWrappers(
        apiPath = apiPath,
        sourceDir = sourceDir,
        createFileGenerator = ::KotlinFileGenerator
    )
}

private fun generateWrappers(
    apiPath: String,
    sourceDir: File,
    createFileGenerator: (types: Iterable<Type>, functionSignatures: Iterable<FunctionSignature>) -> FileGenerator
) {
    val source = JSONObject(loadApiJson(apiPath))

    applyHacks(source)
    correctNumbers(source)

    val apiRoot = ApiRoot(source)
    val types = apiRoot
        .namespaces
        .asSequence()
        .first { it.id == YFILES_NAMESPACE }
        .namespaces
        .flatMap { it.types }
        .filterNot { EXCLUDED_TYPES.contains(it.fqn) }

    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types)

    val fileGenerator = createFileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)
}