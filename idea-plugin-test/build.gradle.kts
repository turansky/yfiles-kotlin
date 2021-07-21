plugins {
    kotlin("js") version "1.5.21"
    id("com.github.turansky.kfc.library") version "4.13.0"
    id("com.github.turansky.yfiles") version "6.7.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:24.0.1-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.1"
}
