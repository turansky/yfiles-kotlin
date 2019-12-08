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
    implementation(kotlin("stdlib-js"))
    implementation(project(":libraries:yfiles-kotlin"))
}
