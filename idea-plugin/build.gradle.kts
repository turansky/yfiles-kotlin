group = "com.github.turansky.yfiles"
version = "0.0.3-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.61"
    id("org.jetbrains.intellij") version "0.4.15"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
}

intellij {
    pluginName = "yfiles"

    version = "2019.3"

    setPlugins("org.jetbrains.kotlin:1.3.61-release-IJ2019.3-1")
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
