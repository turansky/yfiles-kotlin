plugins {
    `java-gradle-plugin`
    `kotlin-dsl`

    id("com.gradle.plugin-publish") version "0.15.0"
    id("com.github.turansky.kfc.plugin-publish") version "4.8.4"

    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("compiler-embeddable"))
}

// TODO: remove after Gradle update
tasks.compileKotlin {
    kotlinOptions.allWarningsAsErrors = false
}

gradlePlugin {
    plugins {
        create("yfiles") {
            id = "com.github.turansky.yfiles"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.YFilesGradleSubplugin"
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
    gradleVersion = "7.0.2"
    distributionType = Wrapper.DistributionType.ALL
}
