package com.github.turansky.yfiles.gradle.plugin

import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact

internal val KOTLIN_PLUGIN_ARTIFACT: SubpluginArtifact
    get() = SubpluginArtifact(
        groupId = "gradle.plugin.com.github.turansky.yfiles",
        artifactId = "gradle-plugin",
        version = "3.3.2-SNAPSHOT"
    )
