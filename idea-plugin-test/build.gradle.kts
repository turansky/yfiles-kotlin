plugins {
    kotlin("js") version "1.4.30"
    id("com.github.turansky.kfc.library") version "2.3.0"
    id("com.github.turansky.yfiles") version "5.6.0"
}

repositories {
    gradlePluginPortal()
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:23.0.4-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "6.8.1"
    distributionType = Wrapper.DistributionType.ALL
}
