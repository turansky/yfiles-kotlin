package com.github.turansky.yfiles.gradle.plugin

import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact

internal val KOTLIN_PLUGIN_ARTIFACT: SubpluginArtifact
    get() = SubpluginArtifact(
        groupId = "com.github.turansky.yfiles",
        artifactId = "gradle-plugin",
        version = "6.20.0-SNAPSHOT"
    )
