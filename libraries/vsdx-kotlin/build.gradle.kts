import com.github.turansky.yfiles.generateVsdxKotlinDeclarations
import de.undercouch.gradle.tasks.download.Download

group = "com.yworks.yfiles"
version = "1.2.0-SNAPSHOT"

plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")

    id("de.undercouch.download")

    id("maven-publish")
}

kotlin.js {
    browser()
}

dependencies {
    implementation(project(":libraries:yfiles-kotlin"))
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
        finalizedBy("publishToMavenLocal")
    }
}

publishing {
    publications {
        register("mavenKotlin", MavenPublication::class) {
            from(components["kotlin"])
            artifact(tasks.getByName("jsSourcesJar"))
        }
    }
}
