plugins {
    kotlin("multiplatform") version "2.3.20" apply false
    kotlin("plugin.js-plain-objects") version "2.3.20" apply false
    id("io.github.turansky.kfc.library") version "18.4.0"
    id("com.github.turansky.yfiles") version "6.20.0"
}

dependencies {
    jsMainImplementation("com.yworks.yfiles:yfiles-kotlin:26.0.4-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "8.9"
}
