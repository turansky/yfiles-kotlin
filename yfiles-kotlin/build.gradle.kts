import com.github.turansky.yfiles.generateKotlinDeclarations
import de.undercouch.gradle.tasks.download.Download

plugins {
    alias(kfc.plugins.library)
    alias(libs.plugins.yfiles)

    alias(libs.plugins.download)
}

dependencies {
    jsMainImplementation(kotlinWrappers.browser)
}

val kotlinSourceDir: File
    get() = kotlin
        .sourceSets
        .get("jsMain")
        .kotlin
        .sourceDirectories
        .first()

tasks {
    clean {
        delete("src")
    }

    val apiDescriptorFile = layout.buildDirectory.file("api.js").get().asFile
    val devguideDescriptorFile = layout.buildDirectory.file("devguide.js").get().asFile

    val downloadApiDescriptor by registering(Download::class) {
        src(project.property("yfiles.api.url"))
        dest(apiDescriptorFile)
        overwrite(true)
    }

    val downloadDevguideDescriptor by registering(Download::class) {
        src(project.property("yfiles.devguide.url"))
        dest(devguideDescriptorFile)
        overwrite(true)
    }

    val generateDeclarations by registering {
        doLast {
            val sourceDir = kotlinSourceDir
                .also { delete(it) }

            generateKotlinDeclarations(
                apiFile = apiDescriptorFile,
                devguideFile = devguideDescriptorFile,
                sourceDir = sourceDir
            )
        }

        dependsOn(downloadApiDescriptor)
        dependsOn(downloadDevguideDescriptor)
    }

    named("compileKotlinJs") {
        dependsOn(generateDeclarations)
    }
}
