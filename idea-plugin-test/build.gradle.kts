plugins {
    kotlin("js") version "1.5.20"
    id("com.github.turansky.kfc.library") version "4.12.0"
    id("com.github.turansky.yfiles") version "6.7.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:24.0.0-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.1"
}
