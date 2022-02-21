plugins {
    kotlin("js") version "1.6.10"
    id("io.github.turansky.kfc.library") version "5.2.1"
    id("com.github.turansky.yfiles") version "6.9.0"
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:24.0.5-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.4"
}
