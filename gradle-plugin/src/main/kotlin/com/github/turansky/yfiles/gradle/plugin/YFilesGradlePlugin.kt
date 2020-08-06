package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class YFilesGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureIdeaCompanion()
        target.configureJsTransformation()
    }
}
