plugins {
    id("io.github.turansky.kfc.library")
    id("com.github.turansky.yfiles")
}

dependencies {
    jsMainImplementation(project(":yfiles-kotlin"))

    jsTestImplementation(kotlin("test-js"))
}
