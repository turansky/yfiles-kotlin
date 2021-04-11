plugins {
    kotlin("jvm") version "1.4.32"
    id("org.jetbrains.intellij") version "0.7.2"
    id("com.github.turansky.kfc.version") version "3.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    pluginName = "yfiles"

    type = "IU"
    version = "2021.1"

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
            allWarningsAsErrors = true
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
