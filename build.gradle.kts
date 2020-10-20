plugins {
    kotlin("js") apply false
}

allprojects {
    repositories {
        jcenter()
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
