package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.applyHacks
import com.github.turansky.yfiles.correction.correctNumbers
import com.github.turansky.yfiles.correction.excludeUnusedTypes
import org.json.JSONObject
import java.io.File
import java.net.URL

private fun loadJson(
    path: String,
    action: JSONObject.() -> Unit
): JSONObject =
    URL(path)
        .readText(DEFAULT_CHARSET)
        .run { substring(indexOf("{")) }
        .run { JSONObject(this) }
        .apply(action)
        .run { toString() }
        .run { JSONObject(this) }

fun generateKotlinDeclarations(
    apiPath: String,
    sourceDir: File
) {
    val source = loadJson(apiPath) {
        applyHacks(this)
        excludeUnusedTypes(this)
        correctNumbers(this)
    }

    val apiRoot = ApiRoot(source)
    val types = apiRoot.types
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types)

    val fileGenerator = KotlinFileGenerator(types, functionSignatures.values)
    fileGenerator.generate(sourceDir)
}