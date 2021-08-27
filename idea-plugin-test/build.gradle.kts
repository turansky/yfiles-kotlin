plugins {
    kotlin("js") version "1.5.30"
    id("com.github.turansky.kfc.library") version "4.30.0"
    id("com.github.turansky.yfiles") version "6.7.0"
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:24.0.2-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.2"
}
