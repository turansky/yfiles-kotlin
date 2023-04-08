plugins {
    kotlin("multiplatform") version "1.8.20" apply false
    id("io.github.turansky.kfc.library") version "7.2.0"
    id("com.github.turansky.yfiles") version "6.20.0"
}

dependencies {
    jsMainImplementation("com.yworks.yfiles:yfiles-kotlin:25.0.2-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "8.0.2"
}
