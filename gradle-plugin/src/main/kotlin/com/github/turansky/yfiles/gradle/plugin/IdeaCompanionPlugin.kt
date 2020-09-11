package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

private const val IDEA_PLUGIN_VERSION: String = "0.18.1"

// language=XML
private val EXTERNAL_DEPENDENCIES: String = """
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ExternalDependencies">
    <plugin id="com.github.turansky.yfiles" min-version="$IDEA_PLUGIN_VERSION" />
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
