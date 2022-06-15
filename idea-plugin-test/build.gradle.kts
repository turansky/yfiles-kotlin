plugins {
    kotlin("js") version "1.7.0"
    id("io.github.turansky.kfc.library") version "5.34.0"
    id("com.github.turansky.yfiles") version "6.11.0"
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:24.0.6-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.4.2"
}
