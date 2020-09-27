rootProject.name = "yfiles-kotlin"

pluginManagement {
    plugins {
        kotlin("js") version "1.4.10"

        val kfcVersion = "0.13.0"
        id("com.github.turansky.kfc.library") version kfcVersion
        id("com.github.turansky.kfc.maven-publish") version kfcVersion

        id("de.undercouch.download") version "4.1.1"
    }
}

include("yfiles-kotlin")
include("vsdx-kotlin")

includeBuild("gradle-plugin")
include("gradle-plugin-test")

include("examples:simple-app")
include("examples:data-classes")
include("examples:cast")
include("examples:configurable-properties")

include("examples:import-optimizer-library")
include("examples:import-optimizer-application")
