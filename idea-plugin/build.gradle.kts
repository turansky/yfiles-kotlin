plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.intellij") version "0.6.5"
    id("com.github.turansky.kfc.version") version "2.3.0"
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
    version = "2020.3.1"

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
        untilBuild("203.*")
    }

    publishPlugin {
        setToken(project.property("intellij.publish.token"))
    }

    wrapper {
        gradleVersion = "6.8"
        distributionType = Wrapper.DistributionType.ALL
    }
}
