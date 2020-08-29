plugins {
    id("com.github.turansky.kfc.library")
    id("com.github.turansky.yfiles")
}

dependencies {
    implementation(project(":yfiles-kotlin"))

    testImplementation(kotlin("test-js"))
}
