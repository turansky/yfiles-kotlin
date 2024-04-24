plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.3"
    id("io.github.turansky.kfc.version") version "7.58.0"
}

repositories {
    mavenCentral()
}

intellij {
    pluginName.set("yfiles")

    type.set("IU")
    version.set("2024.1")

    plugins.set(
        listOf(
            "java",
            "org.jetbrains.kotlin",
            "JavaScript"
        )
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
        sinceBuild.set("241.*")
        untilBuild.set("242.*")
    }

    publishPlugin {
        token.set(project.property("intellij.publish.token") as String)
    }

    buildSearchableOptions {
        enabled = false
    }

    wrapper {
        gradleVersion = "8.5"
    }
}
