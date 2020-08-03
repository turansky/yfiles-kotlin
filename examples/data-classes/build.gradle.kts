plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin.js {
    browser()
}

dependencies {
    implementation(project(":libraries:yfiles-kotlin"))
}
