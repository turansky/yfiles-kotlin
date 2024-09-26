plugins {
    alias(kfc.plugins.library)
    alias(libs.plugins.yfiles)
}

dependencies {
    jsMainImplementation(kotlinWrappers.browser)

    jsMainImplementation(project(":yfiles-kotlin"))
}
