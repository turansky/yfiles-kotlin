plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":yfiles-kotlin"))
}