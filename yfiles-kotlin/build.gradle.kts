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
        .getByName("main")
        .resources
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
            val apiPath = "http://docs.yworks.com/yfileshtml/assets/api.8ff904af.js"
            generateKotlinWrappers(apiPath, kotlinSourceDir)
        }
    }

    compileKotlinJs {
        dependsOn(generateDeclarations)
        finalizedBy("publishToMavenLocal")
    }
}

val mainJar by tasks.registering(Jar::class) {
    from("$projectDir/out")
}

val sourceJar by tasks.registering(Jar::class) {
    from(kotlinSourceDir)
    classifier = "sources"
}

publishing {
    publications {
        register("mavenKotlin", MavenPublication::class) {
            artifact(mainJar.get())
        }

        register("mavenKotlinSources", MavenPublication::class) {
            artifact(sourceJar.get())
        }
    }
}