import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js") version "1.4.0" apply false
    id("com.github.turansky.kfc.webpack") version "0.11.0" apply false
    id("de.undercouch.download") version "4.1.1" apply false
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
                allWarningsAsErrors = true
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "6.6"
    distributionType = Wrapper.DistributionType.ALL
}

// TODO: remove after migration
tasks.register("ttt") {
    dependsOn(project.getTasksByName("compileDevelopmentExecutableKotlinJs", true))
}
