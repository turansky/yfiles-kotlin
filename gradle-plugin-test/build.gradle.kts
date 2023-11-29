plugins {
    id("io.github.turansky.kfc.library")
    id("io.github.turansky.kfc.wrappers")
    id("com.github.turansky.yfiles")
}

dependencies {
    jsMainImplementation(wrappers("browser"))
    jsMainImplementation(project(":yfiles-kotlin"))

    jsTestImplementation(kotlin("test-js"))
}
