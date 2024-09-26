plugins {
    id("io.github.turansky.kfc.library")
    id("com.github.turansky.yfiles")
}

dependencies {
    jsMainImplementation(kotlinWrappers.browser)

    jsMainImplementation(project(":yfiles-kotlin"))
}
