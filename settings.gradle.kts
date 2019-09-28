pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

include("libraries:yfiles-kotlin")
include("libraries:vsdx-kotlin")

include("examples:test-app")

includeBuild("plugins/yfiles")