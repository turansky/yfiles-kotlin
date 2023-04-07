import com.github.turansky.yfiles.generateVsdxKotlinDeclarations
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

    jsMainImplementation(project(":yfiles-kotlin"))
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

    val downloadApiDescriptor by registering(Download::class) {
        src(project.property("vsdx.api.url"))
        dest(apiDescriptorFile)
        overwrite(true)
    }

    val generateDeclarations by registering {
        doLast {
            val sourceDir = kotlinSourceDir
                .also { delete(it) }

            generateVsdxKotlinDeclarations(apiDescriptorFile, sourceDir)
        }

        dependsOn(downloadApiDescriptor)
    }

    named("compileKotlinJs") {
        dependsOn(generateDeclarations)
        finalizedBy(publishToMavenLocal)
    }
}
