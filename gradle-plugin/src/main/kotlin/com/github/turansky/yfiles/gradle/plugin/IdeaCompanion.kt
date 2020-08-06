package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Project

private val COMPANION_CONFIGURED = "yfiles.gradle.plugin.companion.configured"

// language=XML
private val EXTERNAL_DEPENDENCIES = """
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ExternalDependencies">
    <plugin id="com.github.turansky.yfiles" min-version="0.5.0" />
  </component>
</project>    
""".trimIndent()

internal fun Project.configureIdeaCompanion() {
    if (rootProject.hasProperty(COMPANION_CONFIGURED)) {
        return
    }

    rootProject.setProperty(COMPANION_CONFIGURED, true)

    val ideaDirectory = rootDir.resolve(".idea")
        .takeIf { it.isDirectory }
        ?: return

    val dependenciesFile = ideaDirectory.resolve("externalDependencies.xml")
    dependenciesFile.writeText(EXTERNAL_DEPENDENCIES)
}
