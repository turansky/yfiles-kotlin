plugins {
    kotlin("js") version "1.5.10"
    id("com.github.turansky.kfc.library") version "4.9.0"
    id("com.github.turansky.yfiles") version "6.6.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:23.0.4-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.0.2"
}
