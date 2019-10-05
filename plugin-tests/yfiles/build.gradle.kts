plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin {
    target {
        browser()
    }

    sourceSets {
        main {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(project(":libraries:yfiles-kotlin"))
            }
        }
        test {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}