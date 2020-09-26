package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce
import java.io.File

private const val KOTLIN_JS = "org.jetbrains.kotlin.js"

private val Y_IMPORT = Regex("\\\$module\\\$yfiles\\.(\\w+)")
private val Y_INLINE_IMPORT = Regex("yfiles_kotlin\\.\\\$\\\$importsForInline\\\$\\\$\\.yfiles\\.(\\w+)")

internal class ImportOptimizePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId(KOTLIN_JS) {
            target.tasks.withType<KotlinJsDce> {
                doLast {
                    createImportFile(destinationDir)
                }
            }
        }
    }
}

private fun createImportFile(outputDir: File) {
    val jsFiles = outputDir
        .listFiles { _, name -> name.endsWith(".js") && !name.endsWith(".meta.js") }
        ?: return

    val importedClasses = jsFiles.asSequence()
        .flatMap { it.readLines().asSequence() }
        .flatMap { Y_IMPORT.findAll(it) + Y_INLINE_IMPORT.findAll(it) }
        .map { it.groupValues[1] }
        .distinct()
        .toList()

    val imports = importedClasses.joinToString(",\n")
    outputDir.resolve("yfiles.js")
        .writeText("export {\n$imports\n} from '../../../node_modules/yfiles/yfiles.js'")
}
