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

    val types = ApiRoot(source)
            .namespaces.first { it.id == "yfiles" }
            .namespaces.flatMap { it.types }

    ClassRegistry.instance = ClassRegistryImpl(types)

    val fileGenerator = FileGenerator(types)
    fileGenerator.generate(sourceDir)

    sourceDir.resolve("yfiles/lang/Boolean.kt").delete()
    sourceDir.resolve("yfiles/lang/Number.kt").delete()
    sourceDir.resolve("yfiles/lang/String.kt").delete()
    sourceDir.resolve("yfiles/lang/Struct.kt").delete()

    // TODO: generate from signatures
    sourceDir.resolve("yfiles/input/EventRecognizer.kt")
            .writeText(
                    "package yfiles.input\n" +
                            "typealias EventRecognizer = (yfiles.lang.Object, yfiles.lang.EventArgs) -> Boolean",
                    DEFAULT_CHARSET
            )
}