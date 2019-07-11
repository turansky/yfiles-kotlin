package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.applyHacks
import com.github.turansky.yfiles.correction.correctNumbers
import com.github.turansky.yfiles.correction.excludeUnusedTypes
import org.json.JSONObject
import java.io.File
import java.net.URL

private val YFILES_NAMESPACE = "yfiles"

private fun loadApiJson(path: String): String {
    return URL(path)
        .readText(DEFAULT_CHARSET)
        .run { substring(indexOf("{")) }
}

fun generateKotlinWrappers(apiPath: String, sourceDir: File) {
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
    excludeUnusedTypes(source)
    correctNumbers(source)

    val apiRoot = ApiRoot(source)
    val types = apiRoot.types
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types)

    val fileGenerator = createFileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)
}