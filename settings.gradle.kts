pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

include("yfiles-kotlin")
include("test-app")

includeBuild("gradle-plugin")