import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinJsPluginWrapper

plugins {
    kotlin("js") version "1.3.50-eap-86" apply false
}

allprojects {
    repositories {
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlin-eap")
    }
}

subprojects {
    plugins.withType<KotlinJsPluginWrapper> {
        tasks.withType<KotlinJsCompile>().configureEach {
            kotlinOptions {
                moduleKind = "commonjs"
                allWarningsAsErrors = true
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "5.6"
    distributionType = Wrapper.DistributionType.ALL
}