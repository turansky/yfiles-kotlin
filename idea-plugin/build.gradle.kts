plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.16.0"
    id("io.github.turansky.kfc.version") version "7.38.0"
}

repositories {
    mavenCentral()
}

intellij {
    pluginName.set("yfiles")

    type.set("IU")
    version.set("2023.2.5")

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
        sinceBuild.set("232.*")
        untilBuild.set("241.*")
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
