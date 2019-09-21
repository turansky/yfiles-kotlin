group = "com.github.turansky.yfiles"
version = "0.0.6"

plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"

    kotlin("jvm") version "1.3.50"
}

repositories {
    jcenter()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
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
    gradleVersion = "5.6.2"
    distributionType = Wrapper.DistributionType.ALL
}