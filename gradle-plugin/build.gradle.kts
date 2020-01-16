plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"

    kotlin("jvm") version "1.3.61"
    id("com.github.autostyle") version "3.0"
}

repositories {
    jcenter()
}

autostyle {
    kotlin {
        endWithNewline()
    }
}

val kotlinSourceDir: File
    get() = kotlin
        .sourceSets
        .get("main")
        .kotlin
        .sourceDirectories
        .first()

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }

    jar {
        into("META-INF") {
            from("$projectDir/LICENSE.md")
        }
    }

    val preparePublish by registering {
        doLast {
            preparePublish(kotlinSourceDir)
        }
    }

    val prepareDevelopment by registering {
        doLast {
            prepareDevelopment(kotlinSourceDir)
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))
}

gradlePlugin {
    plugins {
        create("yfiles") {
            id = "com.github.turansky.yfiles"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.GradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/turansky/yfiles-kotlin"
    vcsUrl = "https://github.com/turansky/yfiles-kotlin"

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
    gradleVersion = "6.1"
    distributionType = Wrapper.DistributionType.ALL
}
