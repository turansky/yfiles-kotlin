// TODO: Remove after fix https://youtrack.jetbrains.com/issue/KT-34770
package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import java.io.File

private val DESCRIPTOR_REGEX = Regex("(Object\\.defineProperty\\(.+\\.prototype, '[a-zA-Z]+', \\{)")

internal fun String.fixPropertyDeclaration(): String =
    replace(DESCRIPTOR_REGEX, "$1 configurable: true,")


internal fun Project.configureJsTransformation() {
    plugins.withId(KotlinJs.GRADLE_PLUGIN_ID) {
        val compileTasks = tasks.asSequence()
            .filter { it.name in KotlinJs.COMPILE_TASK_NAMES }
            .filterIsInstance<KotlinJsCompile>()
            .toList()

        afterEvaluate {
            for (compileTask in compileTasks) {
                val config = compileTask.addJsTransformation()
                val copyTask = tasks.copyTransformedJs(compileTask.name, config)
                compileTask.finalizedBy(copyTask)
            }
        }
    }
}

private fun KotlinJsCompile.addJsTransformation(): TransformationConfig =
    TransformationConfig(property(KotlinJs.OUTPUT_FILE) as File).apply {
        kotlinOptions.outputFile = tempOutputFile.absolutePath
    }

private fun TaskContainer.copyTransformedJs(
    compileTaskName: String,
    config: TransformationConfig
): TaskProvider<*> =
    register("copyTransformedJs_$compileTaskName", Copy::class.java) {
        val outputFileName = config.originalOutputFile.name

        it.from(config.tempOutputDir) {
            it.include(outputFileName)
            it.filter { it.fixPropertyDeclaration() }
        }

        it.from(config.tempOutputDir) {
            it.exclude(outputFileName)
        }

        it.into(config.originalOutputDir)
    }

private class TransformationConfig(val originalOutputFile: File) {
    val originalOutputDir: File by lazy {
        originalOutputFile.parentFile
    }

    val tempOutputDir: File by lazy {
        originalOutputFile
            .parentFile
            .parentFile
            .resolve("kotlin-temp")
    }

    val tempOutputFile: File by lazy {
        tempOutputDir.resolve(originalOutputFile.name)
    }
}
