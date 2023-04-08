rootProject.name = "yfiles-kotlin"

pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.8.20"

        val kfcVersion = "7.2.0"
        id("io.github.turansky.kfc.library") version kfcVersion

        id("de.undercouch.download") version "5.4.0"
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
