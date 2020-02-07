package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import java.io.File

class YFilesGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId(KotlinJs.GRADLE_PLUGIN_ID) {
            target.tasks.asSequence()
                .filter { it.name == KotlinJs.COMPILE_TASK_NAME }
                .filterIsInstance<KotlinJsCompile>()
                .forEach { it.addJsTransformation() }
        }
    }
}

private fun KotlinJsCompile.addJsTransformation() {
    var configured = false
    val config = TransformationConfig()

    doFirst {
        if (!configured) {
            configured = true

            config.originalOutputFile = property(KotlinJs.OUTPUT_FILE) as File
            kotlinOptions.outputFile = config.tempOutputFile.absolutePath
        }
    }

    doLast {
        config.apply {
            copyDirectory(tempOutputDir, originalOutputDir)

            originalOutputFile.writeText("/* GENERATED CONTENT */\n" + tempOutputFile.readText())
        }
    }
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
