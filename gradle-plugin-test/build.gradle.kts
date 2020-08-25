plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin.js {
    browser()
}

dependencies {
    implementation(project(":yfiles-kotlin"))

    testImplementation(kotlin("test-js"))
}
