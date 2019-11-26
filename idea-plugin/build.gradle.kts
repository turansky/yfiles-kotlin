group = "com.github.turansky.yfiles"
version = "0.0.1-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.60"
    id("org.jetbrains.intellij") version "0.4.14"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
}

intellij {
    pluginName = "yfiles"

    version = "2019.2.4"

    setPlugins("org.jetbrains.kotlin:1.3.60-release-IJ2019.2-1")
}

tasks {
    publishPlugin {
        setToken(project.property("intellij.publish.token"))
    }

    wrapper {
        gradleVersion = "6.0.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}
