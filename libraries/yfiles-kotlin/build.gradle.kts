import com.github.turansky.yfiles.generateKotlinDeclarations

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
            generateKotlinDeclarations(apiPath, sourceDir)
        }
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