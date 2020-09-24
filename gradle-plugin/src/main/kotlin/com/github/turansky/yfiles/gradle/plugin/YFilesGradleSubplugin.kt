package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.*

private val YFILES_COMPILER_PLUGIN_ID = "com.github.turansky.yfiles"

class YFilesGradleSubplugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.plugins.apply(ImportOptimizePlugin::class)

        target.rootProject.plugins.apply(IdeaCompanionPlugin::class)
    }

    override fun isApplicable(
        kotlinCompilation: KotlinCompilation<*>
    ): Boolean =
        kotlinCompilation.target.platformType == KotlinPlatformType.js

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project
            .provider { emptyList() }

    override fun getCompilerPluginId(): String =
        YFILES_COMPILER_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact =
        KOTLIN_PLUGIN_ARTIFACT
}
