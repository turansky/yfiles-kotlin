import com.github.turansky.yfiles.generateKotlinWrappers

group = "com.yworks.yfiles"
version = "2.2.1-SNAPSHOT"

plugins {
    kotlin("js")
    id("maven-publish")
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
        doLast {
            delete("src", "out")
        }
    }

    val generateDeclarations by registering {
        doLast {
            val sourceDir = kotlinSourceDir
            delete(sourceDir)

            val apiPath = "http://docs.yworks.com/yfileshtml/assets/api.8ff904af.js"
            generateKotlinWrappers(apiPath, sourceDir)
        }
    }

    compileKotlinJs {
        dependsOn(generateDeclarations)
        finalizedBy("publishToMavenLocal")
    }
}

publishing {
    publications {
        register("mavenKotlin", MavenPublication::class) {
            artifact(tasks.JsJar.get())
            artifact(tasks.JsSourcesJar.get())
        }
    }
}