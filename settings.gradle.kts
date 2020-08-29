rootProject.name = "yfiles-kotlin"

pluginManagement {
    plugins {
        kotlin("js") version "1.4.0"

        val kfcVersion = "0.12.1"
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
