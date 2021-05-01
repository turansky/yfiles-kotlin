plugins {
    kotlin("jvm") version "1.5.0"
    id("org.jetbrains.intellij") version "0.7.3"
    id("com.github.turansky.kfc.version") version "4.0.0"
}

repositories {
    mavenCentral()
}

intellij {
    pluginName = "yfiles"

    type = "IU"
    version = "2021.1.1"

    setPlugins(
        "java",
        "org.jetbrains.kotlin",
        "JavaScript"
    )
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
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
        sinceBuild("201.6487")
        untilBuild("211.*")
    }

    publishPlugin {
        setToken(project.property("intellij.publish.token"))
    }

    wrapper {
        gradleVersion = "7.0"
        distributionType = Wrapper.DistributionType.ALL
    }
}
