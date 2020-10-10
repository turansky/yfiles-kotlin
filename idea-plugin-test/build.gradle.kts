import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    kotlin("js") version "1.4.10"
    id("com.github.turansky.kfc.library") version "0.16.0"
    id("com.github.turansky.yfiles") version "4.12.0"
}

repositories {
    gradlePluginPortal()
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:23.0.2-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "6.6.1"
    distributionType = Wrapper.DistributionType.ALL
}
