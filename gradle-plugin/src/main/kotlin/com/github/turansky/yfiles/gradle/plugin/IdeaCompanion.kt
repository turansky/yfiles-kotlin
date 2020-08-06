package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Project

// language=XML
private val DEPENDENCIES = """
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ExternalDependencies">
    <plugin id="com.github.turansky.yfiles" min-version="0.5.0" />
  </component>
</project>    
""".trimIndent()


internal fun Project.configureIdeaCompanion() {
    val ideaDirectory = rootDir.resolve(".idea")
        .takeIf { it.isDirectory }
        ?: return

    val dependenciesFile = ideaDirectory.resolve("externalDependencies.xml")
    dependenciesFile.writeText(DEPENDENCIES)
}
