rootProject.name = "gradle-plugin"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://kotlin.bintray.com/kotlin-eap")
    }
}
