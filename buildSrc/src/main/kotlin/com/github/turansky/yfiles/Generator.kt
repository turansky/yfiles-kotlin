package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.*
import com.github.turansky.yfiles.vsdx.correction.applyVsdxHacks
import com.github.turansky.yfiles.vsdx.correction.correctVsdxNumbers
import com.github.turansky.yfiles.vsdx.correction.createVsdxDataClasses
import com.github.turansky.yfiles.vsdx.fakeVsdxInterfaces
import java.io.File

private val GENERATOR_COMMENT = "Automatically generated - do not modify!"

internal val MODULE_NAME = "%module-name%"
internal val DOC_BASE_URL = "%doc-base-url%"

fun generateKotlinDeclarations(
    apiFile: File,
    sourceDir: File
) {
    val source = readJson(apiFile) {
        applyHacks(this)
        excludeUnusedTypes(this)
        correctNumbers(this)
    }

    val apiRoot = ApiRoot(source)
    val types = apiRoot.types
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types)

    val context: GeneratorContext = SimpleGeneratorContext(
        sourceDir = sourceDir,
        moduleName = "yfiles",
        docBaseUrl = "https://docs.yworks.com/yfileshtml"
    )

    val fileGenerator = KotlinFileGenerator(types, functionSignatures.values)
    fileGenerator.generate(context)

    generateIdUtils(context)
    generateBindingUtils(context)
    generateBusinessObjectUtils(context)
    generateTagUtils(context)
    generateStyleTagUtils(context)
    generateResourceUtils(context)
    generateConvertersUtils(context)
    generateEventDispatcherUtils(context)

    generateClassUtils(context)
    generateFlagsUtils(context)
    generateMementoUtils(context)
    generateIncrementalHint(context)
    generatePartitionCellUtils(context)
    generateObstacleData(context)
    generateTooltipUtils(context)
    generateDragDropData(context)

    generateElementIdUtils(context)
    generateCreationPropertyUtils(context)
    generateSerializationUtils(context)
}

fun generateVsdxKotlinDeclarations(
    apiFile: File,
    sourceDir: File
) {
    val source = readJson(apiFile) {
        applyVsdxHacks(this)
        correctVsdxNumbers(this)
    }

    val apiRoot = ApiRoot(source)
    val types = apiRoot.rootTypes
    val functionSignatures = apiRoot.functionSignatures

    ClassRegistry.instance = ClassRegistry(types + fakeVsdxInterfaces())

    val context: GeneratorContext = SimpleGeneratorContext(
        sourceDir = sourceDir,
        moduleName = "yfiles/vsdx",
        docBaseUrl = "https://docs.yworks.com/vsdx-html"
    )

    val fileGenerator = KotlinFileGenerator(types, functionSignatures.values)
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
    private val sourceDir: File,
    private val moduleName: String,
    private val docBaseUrl: String
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
        val text = "// $GENERATOR_COMMENT\n\n" +
                content
                    .replace(MODULE_NAME, moduleName)
                    .replace(DOC_BASE_URL, docBaseUrl)

        sourceDir.resolve(dirPath)
            .also { it.mkdirs() }
            .resolve(fileName)
            .writeText(text)
    }

    override fun clean() {
        sourceDir.mkdirs()
        sourceDir.deleteRecursively()
    }
}
