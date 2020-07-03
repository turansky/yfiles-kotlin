package com.github.turansky.yfiles

import com.github.turansky.yfiles.ContentMode.*
import com.github.turansky.yfiles.correction.*
import com.github.turansky.yfiles.vsdx.correction.applyVsdxHacks
import com.github.turansky.yfiles.vsdx.correction.correctVsdxNumbers
import com.github.turansky.yfiles.vsdx.correction.createVsdxDataClasses
import com.github.turansky.yfiles.vsdx.fakeVsdxInterfaces
import java.io.File

private const val GENERATOR_COMMENT = "Automatically generated - do not modify!"

internal const val DOC_BASE_URL = "%doc-base-url%"

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

    ClassRegistry.instance = ClassRegistry(types)

    val context: GeneratorContext = SimpleGeneratorContext(
        sourceDir = sourceDir,
        moduleName = "yfiles",
        docBaseUrl = "https://docs.yworks.com/yfileshtml"
    )

    val fileGenerator = KotlinFileGenerator(types, apiRoot.functionSignatures)
    fileGenerator.generate(context)

    generateIdUtils(context)
    generateObservableDelegates(context)
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
    generateEdgeDirectednessUtils(context)

    val timeSpanClass = types.first { it.classId == "yfiles.lang.TimeSpan" } as Class
    generateTimeSpanExtensions(context, timeSpanClass)
    generateLookupExtensions(context)

    addIteratorSupport(context)
    generateDpKeyDelegates(context)

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
    val types = apiRoot.types

    ClassRegistry.instance = ClassRegistry(types + fakeVsdxInterfaces())

    val context: GeneratorContext = SimpleGeneratorContext(
        sourceDir = sourceDir,
        moduleName = "vsdx-export-for-yfiles-for-html",
        docBaseUrl = "https://docs.yworks.com/vsdx-html"
    )

    val fileGenerator = KotlinFileGenerator(types, apiRoot.functionSignatures)
    fileGenerator.generate(context)

    createVsdxDataClasses(context)
}

enum class ContentMode {
    CLASS,
    INTERFACE,
    EXTENSIONS,
    DELEGATE,
    ITERATOR,
    ALIASES,
    INLINE
}

internal interface GeneratorContext {
    operator fun set(
        classId: String,
        mode: ContentMode? = null,
        content: String
    )

    fun clean()
}

private const val NOTHING_TO_INLINE = "@file:Suppress(\"NOTHING_TO_INLINE\")\n"

private class SimpleGeneratorContext(
    private val sourceDir: File,
    moduleName: String,
    private val docBaseUrl: String
) : GeneratorContext {
    private val moduleAnnotation = "@file:JsModule(\"$moduleName\")\n\n"

    override fun set(
        classId: String,
        mode: ContentMode?,
        content: String
    ) {
        val packageId = classId.substringBeforeLast(".")
        val className = classId.substringAfterLast(".")
        val dirPath = packageId.replace(".", "/")
        val fileName = when (mode) {
            EXTENSIONS -> "$className.ext"
            DELEGATE -> "$className.delegate"
            ITERATOR -> "$className.iterator"
            ALIASES -> "Aliases"

            else -> className
        } + ".kt"

        val moduleDeclaration = when (mode) {
            CLASS, INTERFACE -> moduleAnnotation
            else -> ""
        }

        val suppresses = when (mode) {
            EXTENSIONS -> if (classId == YOBJECT) NOTHING_TO_INLINE else ""
            DELEGATE -> if (classId != DP_KEY_BASE) NOTHING_TO_INLINE else ""
            INLINE -> NOTHING_TO_INLINE
            else -> ""
        }

        val text = "// $GENERATOR_COMMENT\n\n" +
                moduleDeclaration +
                suppresses +
                "package $packageId\n\n" +
                content.clear(classId)
                    .replace(DOC_BASE_URL, docBaseUrl)

        val file = sourceDir.resolve(dirPath)
            .also { it.mkdirs() }
            .resolve(fileName)

        check(!file.exists())
        file.writeText(text)
    }

    override fun clean() {
        sourceDir.mkdirs()
        sourceDir.deleteRecursively()
    }
}
