plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.3"
    id("io.github.turansky.kfc.version") version "7.58.0"
}

repositories {
    mavenCentral()
}

intellij {
    pluginName = "yfiles"

    type = "IU"
    version = "2024.1"

    plugins = listOf(
        "java",
        "org.jetbrains.kotlin",
        "JavaScript"
    )
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    runIde {
        jvmArgs(
            "-Xms1g",
            "-Xmx4g"
        )
    }

    patchPluginXml {
        sinceBuild = "241.*"
        untilBuild = "242.*"
    }

    publishPlugin {
        token = project.property("intellij.publish.token") as String
    }

    buildSearchableOptions {
        enabled = false
    }

    wrapper {
        gradleVersion = "8.7"
    }
}
