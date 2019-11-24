group = "com.github.turansky.yfiles"
version = "0.0.1-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.60"
    id("org.jetbrains.intellij") version "0.4.13"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
}

tasks.wrapper {
    gradleVersion = "6.0.1"
    distributionType = Wrapper.DistributionType.ALL
}
