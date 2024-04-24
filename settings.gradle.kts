rootProject.name = "yfiles-kotlin"

pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.9.23"

        val kfcVersion = "7.58.0"
        id("io.github.turansky.kfc.library") version kfcVersion
        id("io.github.turansky.kfc.wrappers") version kfcVersion

        id("de.undercouch.download") version "5.5.0"
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
