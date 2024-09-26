plugins {
    alias(kfc.plugins.library)
    alias(libs.plugins.yfiles)
}

dependencies {
    jsMainImplementation(project(":yfiles-kotlin"))
    jsMainImplementation(project(":examples:import-optimizer-library"))
}
