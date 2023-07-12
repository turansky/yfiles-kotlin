plugins {
    kotlin("multiplatform") version "1.8.21" apply false
    id("io.github.turansky.kfc.library") version "7.7.4"
    id("com.github.turansky.yfiles") version "6.20.0"
}

dependencies {
    jsMainImplementation("com.yworks.yfiles:yfiles-kotlin:25.0.4-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "8.2.1"
}
