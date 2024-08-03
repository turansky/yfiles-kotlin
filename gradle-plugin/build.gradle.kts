plugins {
    `kotlin-dsl`

    id("com.gradle.plugin-publish") version "1.0.0"
    id("io.github.turansky.kfc.plugin-publish") version "8.7.0"

    kotlin("jvm") version "2.0.0"
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("compiler-embeddable"))
}

// TODO: remove after migration
tasks.compileKotlin {
    kotlinOptions.freeCompilerArgs += listOf(
        "-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
    )
}

val REPO_URL = "https://github.com/turansky/yfiles-kotlin"

gradlePlugin {
    website.set(REPO_URL)
    vcsUrl.set(REPO_URL)

    plugins {
        create("yfiles") {
            id = "com.github.turansky.yfiles"
            displayName = "yFiles Kotlin/JS plugin"
            description = "yFiles class framework helper for Kotlin/JS"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.YFilesGradleSubplugin"
            tags.set(
                listOf(
                    "yfiles",
                    "kotlin",
                    "kotlin-js",
                    "javascript"
                )
            )
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.9"
}
