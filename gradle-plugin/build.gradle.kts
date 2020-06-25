plugins {
    `java-gradle-plugin`

    id("com.gradle.plugin-publish") version "0.12.0"
    id("com.github.turansky.kfc.plugin-publish") version "0.8.5"

    kotlin("jvm") version "1.4.20-dev-1207"
}

repositories {
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("compiler-embeddable"))
}

pluginPublish {
    gradlePluginPrefix = true
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

// TODO: remove after migration on 1.4
tasks.compileKotlin {
    kotlinOptions.allWarningsAsErrors = false
}

tasks.wrapper {
    gradleVersion = "6.5"
    distributionType = Wrapper.DistributionType.ALL
}
