import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinJsPluginWrapper
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
    plugins.withType<KotlinJsPluginWrapper> {
        tasks.withType<KotlinWebpack>().configureEach {
            sourceMaps = false
        }

        tasks.withType<KotlinJsCompile>().configureEach {
            kotlinOptions {
                moduleKind = "commonjs"
                allWarningsAsErrors = true
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "6.6.1"
    distributionType = Wrapper.DistributionType.ALL
}
