import org.gradle.api.Project
import java.io.File

private val GRADLE_PLUGIN_PREFIX = "gradle.plugin."
private val ARTIFACT_PATH = "com/github/turansky/yfiles/gradle/plugin/KotlinPluginArtifact.kt"

fun Project.preparePublish(sourceDir: File) {
    val version = readVersion()
    val releaseVersion = version.copy(snapshot = false)

    changeVersion(sourceDir, version, releaseVersion)
    group = group.toString().removePrefix(GRADLE_PLUGIN_PREFIX)
}

fun Project.prepareDevelopment(sourceDir: File) {
    val version = readVersion()
    val snapshotVersion = version.copy(
        patch = version.patch + 1,
        snapshot = true
    )

    changeVersion(sourceDir, version, snapshotVersion)
    group = GRADLE_PLUGIN_PREFIX + group.toString()
}

private fun Project.changeVersion(
    sourceDir: File,
    oldVersion: Version,
    newVersion: Version
) {
    writeVersion(newVersion)
    version = newVersion.toString()

    sourceDir.resolve(ARTIFACT_PATH).apply {
        writeText(readText().replace(oldVersion.toString(), newVersion.toString()))
    }
}