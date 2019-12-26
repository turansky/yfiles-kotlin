package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.*
import com.github.turansky.yfiles.vsdx.correction.applyVsdxHacks
import com.github.turansky.yfiles.vsdx.correction.correctVsdxNumbers
import com.github.turansky.yfiles.vsdx.correction.createVsdxDataClasses
import com.github.turansky.yfiles.vsdx.fakeVsdxInterfaces
import java.io.File

fun generateKotlinDeclarations(
    apiFile: File,
    sourceDir: File
) {
    val source = readJson(apiFile) {
        applyHacks(this)
        excludeUnusedTypes(this)
        correctNumbers(this)
    }

    docBaseUrl = "https://docs.yworks.com/yfileshtml"

    val apiRoot = ApiRoot(source)
    val types = apiRoot.types
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types)

    val moduleName = "yfiles"
    val context: GeneratorContext = SimpleGeneratorContext(sourceDir)
    val fileGenerator = KotlinFileGenerator(moduleName, types, functionSignatures.values)
    fileGenerator.generate(context)

    generateIdUtils(context)
    generateBindingUtils(context)
    generateTagUtils(context)
    generateStyleTagUtils(context)
    generateResourceUtils(context)
    generateSerializationUtils(context)
    generateConvertersUtils(context)
    generateEventDispatcherUtils(context)

    generateClassUtils(moduleName, context)
    generateFlagsUtils(context)
    generateIncrementalHint(context)
    generatePartitionCellUtils(context)
}

fun generateVsdxKotlinDeclarations(
    apiFile: File,
    sourceDir: File
) {
    val source = readJson(apiFile) {
        applyVsdxHacks(this)
        correctVsdxNumbers(this)
    }

    docBaseUrl = "https://docs.yworks.com/vsdx-html"

    val apiRoot = ApiRoot(source)
    val types = apiRoot.rootTypes
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types + fakeVsdxInterfaces())

    val context: GeneratorContext = SimpleGeneratorContext(sourceDir)
    val fileGenerator = KotlinFileGenerator("yfiles/vsdx", types, functionSignatures.values)
    fileGenerator.generate(context)

    createVsdxDataClasses(context)
}

internal interface GeneratorContext {
    operator fun set(
        classId: String,
        content: String
    )

    operator fun set(
        dirPath: String,
        fileName: String,
        content: String
    )

    fun clean()
}

private class SimpleGeneratorContext(
    private val sourceDir: File
) : GeneratorContext {
    override fun set(
        classId: String,
        content: String
    ) {
        set(
            dirPath = classId.substringBeforeLast(".").replace(".", "/"),
            fileName = classId.substringAfterLast(".") + ".kt",
            content = content
        )
    }

    override fun set(
        dirPath: String,
        fileName: String,
        content: String
    ) {
        sourceDir.resolve(dirPath)
            .also { it.mkdirs() }
            .resolve(fileName)
            .writeText(content)
    }

    override fun clean() {
        sourceDir.mkdirs()
        sourceDir.deleteRecursively()
    }
}
