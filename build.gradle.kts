import org.gradle.api.GradleException
import java.io.File

task("build") {
    val source = project.properties["apiFile"] ?: throw GradleException("Invalid 'apiFile' parameter value!")
    val file = file(source) ?: throw GradleException("No file located in '$source'")
    generateKotlinWrappers(file)
}

fun generateKotlinWrappers(sourceFile: File) {
    println("Source file - '${sourceFile.absolutePath}'")
}