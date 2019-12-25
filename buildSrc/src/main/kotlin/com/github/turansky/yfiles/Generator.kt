package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.*
import com.github.turansky.yfiles.vsdx.correction.applyVsdxHacks
import com.github.turansky.yfiles.vsdx.correction.correctVsdxNumbers
import com.github.turansky.yfiles.vsdx.correction.createVsdxDataClasses
import com.github.turansky.yfiles.vsdx.fakeVsdxInterfaces
import org.json.JSONObject
import java.io.File

private fun readJson(
    file: File,
    action: JSONObject.() -> Unit
): JSONObject =
    file.readText(DEFAULT_CHARSET)
        .run { substring(indexOf("{")) }
        .run { JSONObject(this) }
        .run { toString() }
        .run { fixSystemPackage() }
        .run { fixClassDeclaration() }
        .run { JSONObject(this) }
        .apply(action)
        .run { toString() }
        .run { JSONObject(this) }

private fun String.fixSystemPackage(): String =
    replace("\"yfiles.system.", "\"yfiles.lang.")
        .replace("\"system.", "\"yfiles.lang.")

private fun String.fixClassDeclaration(): String =
    replace(""""id":"yfiles.lang.Class"""", """"id":"$YCLASS","es6name":"Class"""")
        .replace(""""name":"Class"""", """"name":"YClass"""")
        .replace(""""yfiles.lang.Class"""", """"$YCLASS"""")
        .replace(""""Array<yfiles.lang.Class>"""", """"Array<$YCLASS>"""")
        .replace(""""yfiles.collections.Map<yfiles.lang.Class,$JS_OBJECT>"""", """"yfiles.collections.Map<$YCLASS,$JS_OBJECT>"""")

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
    val fileGenerator = KotlinFileGenerator(moduleName, types, functionSignatures.values)
    fileGenerator.generate(sourceDir)

    generateIdUtils(sourceDir)
    generateBindingUtils(sourceDir)
    generateTagUtils(sourceDir)
    generateStyleTagUtils(sourceDir)
    generateResourceUtils(sourceDir)
    generateSerializationUtils(sourceDir)
    generateConvertersUtils(sourceDir)
    generateEventDispatcherUtils(sourceDir)

    generateClassUtils(moduleName, sourceDir)
    generateFlagsUtils(sourceDir)
    generateIncrementalHint(sourceDir)
    generatePartitionCellUtils(sourceDir)
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

    val fileGenerator = KotlinFileGenerator("yfiles/vsdx", types, functionSignatures.values)
    fileGenerator.generate(sourceDir)

    createVsdxDataClasses(sourceDir)
}
