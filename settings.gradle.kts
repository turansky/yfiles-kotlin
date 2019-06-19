pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/kotlin-dev")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin2js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}

include("api")
include("test-app")

project(":api").name = "yfiles-kotlin"