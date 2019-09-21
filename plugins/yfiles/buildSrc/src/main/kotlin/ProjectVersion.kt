import nu.studer.java.util.OrderedProperties
import org.gradle.api.Project
import java.io.File

internal fun Project.readVersion(): Version =
    parseVersion(version.toString())

internal fun Project.writeVersion(version: Version) {
    val properties = OrderedProperties.OrderedPropertiesBuilder()
        .withSuppressDateInComment(true)
        .build()

    val file = File("gradle.properties")
    properties.load(file.inputStream())

    properties.setProperty("version", version.toString())
    properties.store(file.writer(), null)


}