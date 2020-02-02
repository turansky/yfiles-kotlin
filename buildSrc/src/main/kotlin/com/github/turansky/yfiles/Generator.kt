package com.github.turansky.yfiles

import com.github.turansky.yfiles.ContentMode.ALIASES
import com.github.turansky.yfiles.ContentMode.EXTENSIONS
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
    devguideFile: File,
    sourceDir: File
) {
    val source = apiFile.readApiJson {
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
    generateDataTagUtils(context)
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

    generateResourceTypes(devguideFile.readJson(), context)
}

fun generateVsdxKotlinDeclarations(
    apiFile: File,
    sourceDir: File
) {
    val source = apiFile.readApiJson {
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

enum class ContentMode {
    DEFAULT,
    EXTENSIONS,
    ALIASES
}

internal interface GeneratorContext {
    operator fun set(
        classId: String,
        mode: ContentMode? = null,
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
        mode: ContentMode?,
        content: String
    ) {
        val packageId = classId.substringBeforeLast(".")
        val dirPath = packageId.replace(".", "/")
        val fileName = when (mode) {
            EXTENSIONS -> classId.substringAfterLast(".") + ".ext"
            ALIASES -> "Aliases"

            else -> classId.substringAfterLast(".")
        } + ".kt"

        val packageDeclaration = if ("package yfiles." !in content) {
            "package $packageId\n\n"
        } else {
            ""
        }

        val text = "// $GENERATOR_COMMENT\n\n" +
                packageDeclaration +
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
