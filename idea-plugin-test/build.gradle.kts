import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    kotlin("js") version "1.4.0"
    id("com.github.turansky.yfiles") version "3.2.0"
}

repositories {
    gradlePluginPortal()
    jcenter()
    mavenLocal()
}

kotlin.js {
    nodejs()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("com.yworks.yfiles:yfiles-kotlin:23.0.2-SNAPSHOT")
}

tasks {
    withType<KotlinJsCompile>().configureEach {
        kotlinOptions {
            moduleKind = "commonjs"
            allWarningsAsErrors = true
        }
    }

    wrapper {
        gradleVersion = "6.6"
        distributionType = Wrapper.DistributionType.ALL
    }
}
