rootProject.name = "yfiles-gradle-plugin"

pluginManagement {
    repositories {
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlin-eap")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.jvm") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}