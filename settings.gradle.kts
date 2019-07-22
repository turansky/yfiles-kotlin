pluginManagement {
    repositories {
        jcenter()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}

include("yfiles-kotlin")
include("test-app")

includeBuild("gradle-plugin")