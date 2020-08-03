plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
    id("com.github.turansky.kfc.webpack")
}

kotlin.js {
    browser()

    binaries.executable()
}

dependencies {
    implementation(project(":libraries:yfiles-kotlin"))
}
