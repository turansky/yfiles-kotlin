plugins {
    kotlin("js") apply false
}

allprojects {
    repositories {
        jcenter()
        maven("https://kotlin.bintray.com/kotlin-eap")
    }
}

tasks.wrapper {
    gradleVersion = "6.7"
    distributionType = Wrapper.DistributionType.ALL
}

// TODO: remove after migration
tasks.register("ttt") {
    dependsOn(project.getTasksByName("compileDevelopmentExecutableKotlinJs", true))
}
