plugins {
    kotlin("js") version "1.7.21"
    id("io.github.turansky.kfc.library") version "5.70.0"
    id("com.github.turansky.yfiles") version "6.20.0"
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:25.0.2-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "7.5.1"
}
