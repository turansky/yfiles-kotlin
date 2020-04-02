plugins {
    `java-gradle-plugin`
    `kotlin-dsl`

    id("com.gradle.plugin-publish") version "0.11.0"
    id("com.github.turansky.kfc.plugin-publish") version "0.7.6"

    kotlin("jvm") version "1.3.71"
}

repositories {
    jcenter()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

val kotlinSourceDir: File
    get() = kotlin
        .sourceSets
        .get("main")
        .kotlin
        .sourceDirectories
        .first()

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("compiler-embeddable"))
}

pluginPublish {
    gradlePluginPrefix = true
    versionFiles = listOf(
        kotlinSourceDir.resolve("com/github/turansky/yfiles/gradle/plugin/KotlinPluginArtifact.kt")
    )
}

gradlePlugin {
    plugins {
        create("yfiles") {
            id = "com.github.turansky.yfiles"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.YFilesGradlePlugin"
        }
    }
}

val REPO_URL = "https://github.com/turansky/yfiles-kotlin"

pluginBundle {
    website = REPO_URL
    vcsUrl = REPO_URL

    plugins.getByName("yfiles") {
        displayName = "yFiles Kotlin/JS plugin"
        description = "yFiles class framework helper for Kotlin/JS"
        tags = listOf(
            "yfiles",
            "kotlin",
            "kotlin-js",
            "javascript"
        )
        version = project.version.toString()
    }
}

tasks.wrapper {
    gradleVersion = "6.3"
    distributionType = Wrapper.DistributionType.ALL
}
