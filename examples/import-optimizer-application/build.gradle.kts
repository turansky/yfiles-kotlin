import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
    id("com.github.turansky.yfiles")
}

kotlin.js {
    browser()
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
    implementation(project(":yfiles-kotlin"))
    implementation(project(":examples:import-optimizer-library"))
}
