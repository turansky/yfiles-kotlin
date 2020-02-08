// TODO: Remove after fix https://youtrack.jetbrains.com/issue/KT-34770
package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinJsPluginWrapper
import java.io.File

private val DESCRIPTOR_REGEX = Regex("(Object\\.defineProperty\\(.+\\.prototype, '[a-zA-Z]+', \\{)")

private fun String.fixPropertyDeclaration(): String =
    replace(DESCRIPTOR_REGEX, "$1 configurable: true,")


internal fun Project.configureJsTransformation() {
    plugins.withType<KotlinJsPluginWrapper> {
        // wait for Kotlin target configuration
        afterEvaluate {
            val compileTasks = tasks.withType<KotlinJsCompile>()
                .filter { it.name in KotlinJs.COMPILE_TASK_NAMES }

            for (compileTask in compileTasks) {
                val jsTarget = compileTask.kotlinOptions.target
                if (jsTarget != KotlinJs.TARGET_V5) {
                    logger.warn("Unsupported JS target '$jsTarget'. Fix for KT-34770 won't be applied!")
                    continue
                }

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
    register("copyTransformedJs_$compileTaskName", Copy::class) {
        val outputFileName = config.originalOutputFile.name

        from(config.tempOutputDir) {
            include(outputFileName)
            filter { line -> line.fixPropertyDeclaration() }
        }

        from(config.tempOutputDir) {
            exclude(outputFileName)
        }

        into(config.originalOutputDir)
    }

private class TransformationConfig(val originalOutputFile: File) {
    val originalOutputDir: File = originalOutputFile.parentFile

    val tempOutputDir: File = originalOutputFile
        .parentFile
        .parentFile
        .resolve("kotlin-temp")

    val tempOutputFile: File = tempOutputDir.resolve(originalOutputFile.name)
}
