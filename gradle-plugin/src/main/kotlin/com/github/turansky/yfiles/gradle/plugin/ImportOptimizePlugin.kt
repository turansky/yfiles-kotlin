package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import java.io.File

private val Y_IMPORT = Regex("\\\$module\\\$yfiles\\.(\\w+)")

internal class ImportOptimizePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId("org.jetbrains.kotlin.js") {
            target.tasks.withType<KotlinJsCompile> {
                doLast {
                    if (kotlinOptions.target == "v5") {
                        createImportFile(kotlinOptions.outputFile!!)
                    }
                }
            }
        }
    }
}

private fun createImportFile(outputPath: String) {
    val outputFile = File(outputPath)
    val importedClasses = outputFile.readLines().asSequence()
        .flatMap { Y_IMPORT.findAll(it) }
        .map { it.groups.get(1)!!.value }
        .distinct()
        .toList()

    val imports = importedClasses.joinToString(",\n")
    outputFile.parentFile.resolve("yfiles.js")
        .writeText("$imports,")
}
