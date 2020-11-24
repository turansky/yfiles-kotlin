plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jetbrains.intellij") version "0.6.4"
    id("com.github.turansky.kfc.version") version "1.0.0"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    pluginName = "yfiles"

    type = "IU"
    version = "2020.2.3"

    setPlugins(
        "java",
        "org.jetbrains.kotlin:1.4.20-release-IJ2020.2-1",
        "JavaScript"
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

    runIde {
        jvmArgs(
            "-Xms1g",
            "-Xmx4g"
        )
    }

    patchPluginXml {
        sinceBuild("201.6487")
        untilBuild("203.*")
    }

    publishPlugin {
        setToken(project.property("intellij.publish.token"))
    }

    wrapper {
        gradleVersion = "6.7.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}
