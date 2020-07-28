plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

dependencies {
    implementation(project(":libraries:yfiles-kotlin"))
}
