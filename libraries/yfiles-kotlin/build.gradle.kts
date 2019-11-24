import com.github.turansky.yfiles.generateKotlinDeclarations
import de.undercouch.gradle.tasks.download.Download

group = "com.yworks.yfiles"
version = "22.0.2-SNAPSHOT"

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
        src("http://docs.yworks.com/yfileshtml/assets/api.f79373c0.js")
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

    JsJar {
        from(project.projectDir) {
            include("package.json")
        }
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
