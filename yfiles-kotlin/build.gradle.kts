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

tasks.clean {
    doLast {
        delete("src", "out")
    }
}

val generateDeclarations by tasks.register("generateDeclarations") {
    doLast {
        val apiPath = "http://docs.yworks.com/yfileshtml/assets/api.8ff904af.js"
        generateKotlinWrappers(apiPath, File(projectDir, "src/main/kotlin"))
    }
}

tasks.compileKotlinJs {
    kotlinOptions {
        outputFile = "$projectDir/out/yfiles-kotlin.js"
        moduleKind = "amd"
        metaInfo = true
    }

    dependsOn(generateDeclarations)
    finalizedBy("publishToMavenLocal")
}

val mainJar by tasks.registering(Jar::class) {
    from("$projectDir/out")
}

val sourceJar by tasks.registering(Jar::class) {
    from("$projectDir/src/main/kotlin")
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