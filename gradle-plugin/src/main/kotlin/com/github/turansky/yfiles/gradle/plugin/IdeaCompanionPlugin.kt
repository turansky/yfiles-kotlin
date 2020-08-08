package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

// language=XML
private val EXTERNAL_DEPENDENCIES = """
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ExternalDependencies">
    <plugin id="com.github.turansky.yfiles" min-version="0.5.0" />
  </component>
</project>    
""".trimIndent()

internal class IdeaCompanionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ideaDirectory = target.rootDir
            .resolve(".idea")
            .takeIf { it.isDirectory }
            ?: return

        ideaDirectory.resolve("externalDependencies.xml")
            .writeText(EXTERNAL_DEPENDENCIES)
    }
}
