import com.github.turansky.yfiles.generateKotlinDeclarations
import de.undercouch.gradle.tasks.download.Download

group = "com.yworks.yfiles"
version = "22.0.3-SNAPSHOT"

plugins {
    kotlin("js")
    id("de.undercouch.download")

    id("maven-publish")
}

kotlin {
    target {
        nodejs()
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

val kotlinSourceDir: File
    get() = kotlin
        .sourceSets
        .get("main")
        .kotlin
        .sourceDirectories
        .singleFile

tasks {
    clean {
        delete("src")
    }

    val apiDescriptorFile = File(buildDir, "api.js")

    val downloadApiDescriptor by registering(Download::class) {
        src(project.property("yfiles.api.url"))
        dest(apiDescriptorFile)
        overwrite(true)
    }

    val generateDeclarations by registering {
        doLast {
            val sourceDir = kotlinSourceDir
                .also { delete(it) }

            generateKotlinDeclarations(apiDescriptorFile, sourceDir)
        }

        dependsOn(downloadApiDescriptor)
    }

    compileKotlinJs {
        dependsOn(generateDeclarations)
        finalizedBy("publishToMavenLocal")
    }
}

publishing {
    publications {
        register("mavenKotlin", MavenPublication::class) {
            from(components["kotlin"])
            artifact(tasks.JsSourcesJar.get())
        }
    }
}
