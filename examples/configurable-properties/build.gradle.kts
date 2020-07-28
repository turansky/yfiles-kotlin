plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin {
    target {
        nodejs()
    }
}

dependencies {
    implementation(project(":libraries:yfiles-kotlin"))
}
