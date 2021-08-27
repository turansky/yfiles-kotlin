plugins {
    kotlin("js") apply false
}

tasks.wrapper {
    gradleVersion = "7.2"
}

// TODO: remove after migration
tasks.register("ttt") {
    dependsOn(project.getTasksByName("compileDevelopmentExecutableKotlinJs", true))
}
