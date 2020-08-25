plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
    id("com.github.turansky.kfc.webpack")
}

kotlin.js {
    browser()
}

dependencies {
    implementation(project(":yfiles-kotlin"))

    testImplementation(kotlin("test-js"))
}
