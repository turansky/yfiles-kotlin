package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import java.io.File

class YFilesGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId(KotlinJs.GRADLE_PLUGIN_ID) {
            val compileTasks = target.tasks.asSequence()
                .filter { it.name == KotlinJs.COMPILE_TASK_NAME }
                .filterIsInstance<KotlinJsCompile>()
                .toList()

            target.afterEvaluate {
                for (compileTask in compileTasks) {
                    val config = TransformationConfig()
                    compileTask.addJsTransformation(config)
                    val copyTask = target.tasks.copyTransformedJs(config)
                    compileTask.finalizedBy(copyTask)
                }
            }
        }
    }
}

private fun KotlinJsCompile.addJsTransformation(config: TransformationConfig) {
    config.originalOutputFile = property(KotlinJs.OUTPUT_FILE) as File
    kotlinOptions.outputFile = config.tempOutputFile.absolutePath
}

private fun TaskContainer.copyTransformedJs(config: TransformationConfig): TaskProvider<*> =
    register("copyTransformedJs", Copy::class.java) {
        it.from(config.tempOutputDir)
        it.into(config.originalOutputDir)
    }

private class TransformationConfig {
    lateinit var originalOutputFile: File

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
