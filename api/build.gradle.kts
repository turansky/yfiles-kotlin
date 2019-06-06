import com.github.turansky.yfiles.generateKotlinWrappers

group = "com.yworks.yfiles"
version = "2.2.0-SNAPSHOT"

plugins {
    id("kotlin2js")
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
    val apiPath = "http://docs.yworks.com/yfileshtml/assets/api.8ff904af.js"
    generateKotlinWrappers(apiPath, File(projectDir, "src/main/kotlin"))
}

tasks.compileKotlin2Js {
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
    from(sourceSets.main.get().allSource)
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