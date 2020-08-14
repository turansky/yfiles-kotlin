plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin.js {
    nodejs()
}

dependencies {
    implementation(project(":libraries:yfiles-kotlin"))
}
