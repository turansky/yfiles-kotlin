plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.intellij") version "1.1.4"
    id("com.github.turansky.kfc.version") version "4.21.0"
}

repositories {
    mavenCentral()
}

intellij {
    pluginName.set("yfiles")

    type.set("IU")
    version.set("2021.1.3")

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
        kotlinOptions {
            jvmTarget = "11"
            // TODO: restore after Gradle update
            allWarningsAsErrors = false
        }
    }

    runIde {
        jvmArgs(
            "-Xms1g",
            "-Xmx4g"
        )
    }

    patchPluginXml {
        sinceBuild.set("201.6487")
        untilBuild.set("214.*")
    }

    publishPlugin {
        token.set(project.property("intellij.publish.token") as String)
    }

    wrapper {
        gradleVersion = "7.2"
    }
}
