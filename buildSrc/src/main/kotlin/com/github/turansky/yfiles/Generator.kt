package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.applyHacks
import com.github.turansky.yfiles.correction.correctNumbers
import com.github.turansky.yfiles.correction.excludeUnusedTypes
import org.json.JSONObject
import java.io.File
import java.net.URL

private fun loadApiJson(path: String): String =
    URL(path)
        .readText(DEFAULT_CHARSET)
        .run { substring(indexOf("{")) }
        .run { JSONObject(this) }
        .apply(::applyHacks)
        .apply(::excludeUnusedTypes)
        .apply(::correctNumbers)
        .run { toString() }

fun generateKotlinDeclarations(apiPath: String, sourceDir: File) {
    generateWrappers(
        apiPath = apiPath,
        sourceDir = sourceDir
    )
}

private fun generateWrappers(
    apiPath: String,
    sourceDir: File
) {
    val source = JSONObject(loadApiJson(apiPath))

    val apiRoot = ApiRoot(source)
    val types = apiRoot.types
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types)

    val fileGenerator = KotlinFileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)
}