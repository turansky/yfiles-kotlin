import com.github.turansky.yfiles.generateVsdxKotlinDeclarations

group = "com.yworks.yfiles"
version = "1.0.0-SNAPSHOT"

plugins {
    kotlin("js")
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
            delete("src", "out")
        }
    }

    val generateDeclarations by registering {
        doLast {
            val sourceDir = kotlinSourceDir
            delete(sourceDir)

            val apiPath = "https://docs.yworks.com/vsdx-html/assets/api.56a9cdca.js"
            generateVsdxKotlinDeclarations(apiPath, sourceDir)
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