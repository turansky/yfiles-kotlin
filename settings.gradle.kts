rootProject.name = "yfiles-kotlin"

pluginManagement {
    plugins {
        kotlin("js") version "1.7.20"

        val kfcVersion = "5.65.0"
        id("io.github.turansky.kfc.library") version kfcVersion
        id("io.github.turansky.kfc.maven-publish") version kfcVersion

        id("de.undercouch.download") version "5.1.0"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("yfiles-kotlin")
include("vsdx-kotlin")

includeBuild("gradle-plugin")
include("gradle-plugin-test")

include("examples:simple-app")
include("examples:data-classes")
include("examples:configurable-properties")

include("examples:import-optimizer-library")
include("examples:import-optimizer-application")
