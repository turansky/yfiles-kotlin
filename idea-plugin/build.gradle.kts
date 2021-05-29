plugins {
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.intellij") version "1.0"
    id("com.github.turansky.kfc.version") version "4.9.0"
}

repositories {
    mavenCentral()
}

intellij {
    pluginName.set("yfiles")

    type.set("IU")
    version.set("2021.1.1")

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
        gradleVersion = "7.0.2"
        distributionType = Wrapper.DistributionType.ALL
    }
}
