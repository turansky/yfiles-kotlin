plugins {
    kotlin("js") version "1.6.10"
    id("com.github.turansky.kfc.library") version "4.61.0"
    id("com.github.turansky.yfiles") version "6.7.0"
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:24.0.4-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.3.3"
}
