package com.github.turansky.yfiles.gradle.plugin

import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact

internal val KOTLIN_PLUGIN_ARTIFACT: SubpluginArtifact
    get() = SubpluginArtifact(
        groupId = "gradle.plugin.com.github.turansky.yfiles",
        artifactId = "yfiles",
        version = "0.2.0-SNAPSHOT"
    )
