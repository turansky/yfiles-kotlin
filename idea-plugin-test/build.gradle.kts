plugins {
    kotlin("multiplatform") version "1.9.23" apply false
    id("io.github.turansky.kfc.library") version "7.58.0"
    id("com.github.turansky.yfiles") version "6.20.0"
}

dependencies {
    jsMainImplementation("com.yworks.yfiles:yfiles-kotlin:26.0.3-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "8.5"
}
