import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinJsPluginWrapper

plugins {
    kotlin("js") version "1.3.41" apply false
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    plugins.withType<KotlinJsPluginWrapper> {
        tasks.withType<KotlinJsCompile>().configureEach {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "5.5"
    distributionType = Wrapper.DistributionType.ALL
}