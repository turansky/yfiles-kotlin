@file:JvmName("Generator")

package com.yworks.yfiles.api.generator

import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

fun generateKotlinWrappers(sourceData: String, sourceDir: File) {
    val source = JSONObject(sourceData)

    val types = JAPIRoot(source)
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
                    Charset.forName("UTF-8")
            )

    sourceDir.resolve("system").mkdir()
    sourceDir.resolve("system/Comparison.kt")
            .writeText(
                    "package system\n" +
                            "typealias Comparison<T> = (T, T) -> Number",
                    Charset.forName("UTF-8")
            )
}