plugins {
    alias(kfc.plugins.library)
    id("com.github.turansky.yfiles")
}

dependencies {
    jsMainImplementation(project(":yfiles-kotlin"))
}
