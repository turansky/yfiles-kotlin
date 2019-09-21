pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

include("yfiles-kotlin")
include("vsdx-kotlin")
include("test-app")

includeBuild("plugins/yfiles")