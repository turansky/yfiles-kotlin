import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin.js {
    browser {
        dceTask {
            keep("yfiles-kotlin-import-optimizer-application")
        }
    }

    binaries.executable()
}

tasks {
    withType<KotlinJsCompile> {
        kotlinOptions.moduleKind = "commonjs"
    }

    withType<KotlinWebpack> {
        enabled = false
    }
}

dependencies {
    jsMainImplementation(project(":yfiles-kotlin"))
    jsMainImplementation(project(":examples:import-optimizer-library"))
}
