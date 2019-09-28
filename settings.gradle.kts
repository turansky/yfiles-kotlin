pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

include("libraries:yfiles-kotlin")
include("libraries:vsdx-kotlin")

includeBuild("plugins/yfiles")
include("plugin-tests:yfiles")

include("examples:simple-app")