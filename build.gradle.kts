import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js") version "1.4-M2-eap-75" apply false
    id("com.github.turansky.kfc.webpack") version "0.8.5" apply false
    id("de.undercouch.download") version "4.0.4" apply false
}

allprojects {
    repositories {
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
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
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "6.4"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("ttt") {
    dependsOn(project.getTasksByName("compileDevelopmentExecutableKotlinJs", true))
}
