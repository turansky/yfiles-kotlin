plugins {
    alias(kfc.plugins.library)
    id("com.github.turansky.yfiles")
}

dependencies {
    jsMainImplementation(kotlinWrappers.browser)
    jsMainImplementation(project(":yfiles-kotlin"))

    jsTestImplementation(kotlin("test-js"))
}
