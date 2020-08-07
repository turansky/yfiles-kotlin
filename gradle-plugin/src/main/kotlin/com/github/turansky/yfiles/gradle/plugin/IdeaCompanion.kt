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

@Suppress("UNCHECKED_CAST")
internal fun Project.configureIdeaCompanion() {
    rootProject.properties
        .let { it as MutableMap<String, Any?> }
        .putIfAbsent(COMPANION_CONFIGURED, true)
        ?: return

    val ideaDirectory = rootDir.resolve(".idea")
        .takeIf { it.isDirectory }
        ?: return

    ideaDirectory.resolve("externalDependencies.xml")
        .writeText(EXTERNAL_DEPENDENCIES)
}
