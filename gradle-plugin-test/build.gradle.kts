plugins {
    alias(kfc.plugins.library)
    alias(libs.plugins.yfiles)
}

dependencies {
    jsMainImplementation(kotlinWrappers.browser)
    jsMainImplementation(project(":yfiles-kotlin"))
    jsTestImplementation(project(":yfiles-kotlin"))
    jsTestImplementation(kotlin("test-js"))
}

kotlin {
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useFirefox()
                }
            }
        }
    }
}