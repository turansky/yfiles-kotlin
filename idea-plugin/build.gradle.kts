group = "com.github.turansky.yfiles"
version = "0.0.14-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.61"
    id("org.jetbrains.intellij") version "0.4.16"
    id("com.github.autostyle") version "3.0"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
}

intellij {
    pluginName = "yfiles"

    version = "2019.3.2"

    setPlugins(
        "gradle",
        "java", // TODO: check why depend is required for build
        "org.jetbrains.kotlin:1.3.61-release-IJ2019.3-1"
    )
}

autostyle {
    kotlin {
        endWithNewline()
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
            allWarningsAsErrors = true
        }
    }

    patchPluginXml {
        setUntilBuild("201.*")
    }

    publishPlugin {
        setToken(project.property("intellij.publish.token"))
    }

    wrapper {
        gradleVersion = "6.1.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}
