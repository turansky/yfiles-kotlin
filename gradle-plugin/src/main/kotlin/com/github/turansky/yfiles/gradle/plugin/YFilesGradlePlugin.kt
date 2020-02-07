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
    lateinit var originalOutputFile: File
    lateinit var tempOutputFile: File

    doFirst {
        originalOutputFile = property("outputFile") as File
        tempOutputFile = originalOutputFile.parentFile.resolve("temp.js")
        println("START OUTPUT FILE: $originalOutputFile")
        println("START TEMP OUTPUT FILE: $tempOutputFile")

        // TODO: fix for webpack tests
        // kotlinOptions.outputFile = tempOutputFile.absolutePath
    }

    doLast {
        val outputFile = property("outputFile") as File
        println("END OUTPUT FILE: $outputFile")

        originalOutputFile.writeBytes(tempOutputFile.readBytes())
    }
}
