group = "com.github.turansky.yfiles"
version = "0.15.2-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.0"
    id("org.jetbrains.intellij") version "0.4.21"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    pluginName = "yfiles"

    version = "2020.2.1"

    setPlugins(
        "java",
        "org.jetbrains.kotlin:1.4.0-release-IJ2020.2-1"
    )
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            // TODO: uncomment after Gradle update on Kotlin 1.4
            // allWarningsAsErrors = true
        }
    }

    patchPluginXml {
        sinceBuild("201.6487")
        untilBuild("203.*")
    }

    publishPlugin {
        setToken(project.property("intellij.publish.token"))
    }

    wrapper {
        gradleVersion = "6.6.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}
