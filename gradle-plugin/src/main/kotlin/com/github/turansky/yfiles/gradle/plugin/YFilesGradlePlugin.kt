package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

class YFilesGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId(KotlinJs.GRADLE_PLUGIN_ID) {
            target.tasks.asSequence()
                .filter { it.name == KotlinJs.COMPILE_TASK_NAME }
                .forEach { it.addJsTransformation() }
        }
    }
}

private fun Task.addJsTransformation() {
    doLast {
        val outputFile = property("outputFile") as File
        println("OUTPUT FILE: $outputFile")
    }
}
