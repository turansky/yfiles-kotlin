import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js") apply false
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    // TODO: use plugins after regression fix
    //  https://youtrack.jetbrains.com/issue/KT-38203
    afterEvaluate {
        tasks.withType<KotlinWebpack>().configureEach {
            sourceMaps = false
        }

        tasks.withType<KotlinJsCompile>().configureEach {
            kotlinOptions {
                moduleKind = "commonjs"
                // TODO: activate after source map support fix
                allWarningsAsErrors = false
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "6.6.1"
    distributionType = Wrapper.DistributionType.ALL
}

// TODO: remove after migration
tasks.register("ttt") {
    dependsOn(project.getTasksByName("compileDevelopmentExecutableKotlinJs", true))
}
