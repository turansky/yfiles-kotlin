import com.github.turansky.yfiles.generateKotlinDeclarations
import de.undercouch.gradle.tasks.download.Download

plugins {
    id("io.github.turansky.kfc.library")
    id("com.github.turansky.yfiles")

    id("de.undercouch.download")
}

val kotlinWrappersVersion = property("kotlin-wrappers.version") as String

dependencies {
    api(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:$kotlinWrappersVersion"))
    api("org.jetbrains.kotlin-wrappers:kotlin-browser")
}

val kotlinSourceDir: File
    get() = kotlin
        .sourceSets
        .get("main")
        .kotlin
        .sourceDirectories
        .first()

tasks {
    clean {
        delete("src")
    }

    val apiDescriptorFile = File(buildDir, "api.js")
    val devguideDescriptorFile = File(buildDir, "devguide.js")

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
        finalizedBy(publishToMavenLocal)
    }
}
