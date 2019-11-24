import com.github.turansky.yfiles.generateVsdxKotlinDeclarations
import de.undercouch.gradle.tasks.download.Download

group = "com.yworks.yfiles"
version = "1.0.0-SNAPSHOT"

plugins {
    kotlin("js")
    id("de.undercouch.download")

    id("maven-publish")
}

kotlin {
    target {
        nodejs()
    }

    sourceSets {
        main {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(project(":libraries:yfiles-kotlin"))
            }
        }
    }
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
        doLast {
            delete("src")
        }
    }

    val apiDescriptorFile = File(buildDir, "api.js")

    val downloadApiDescriptor by registering(Download::class) {
        src("https://docs.yworks.com/vsdx-html/assets/api.56a9cdca.js")
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
