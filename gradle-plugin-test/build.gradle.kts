plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin {
    target {
        browser()
    }
}

dependencies {
    implementation(project(":libraries:yfiles-kotlin"))

    testImplementation(kotlin("test-js"))
}
