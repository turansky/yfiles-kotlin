package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class YFilesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Hallo from yFiles plugin!")
    }
}