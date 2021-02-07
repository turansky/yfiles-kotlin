plugins {
    kotlin("js") apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.wrapper {
    gradleVersion = "6.8.2"
    distributionType = Wrapper.DistributionType.ALL
}

// TODO: remove after migration
tasks.register("ttt") {
    dependsOn(project.getTasksByName("compileDevelopmentExecutableKotlinJs", true))
}
