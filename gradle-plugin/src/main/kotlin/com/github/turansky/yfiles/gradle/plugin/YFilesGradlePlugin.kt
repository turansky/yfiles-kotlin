package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class YFilesGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.rootProject.plugins.apply(IdeaCompanionPlugin::class)

        target.configureJsTransformation()
    }
}
