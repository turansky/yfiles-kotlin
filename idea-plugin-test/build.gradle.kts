plugins {
    kotlin("js") version "1.4.10"
    id("com.github.turansky.kfc.library") version "1.3.0"
    id("com.github.turansky.yfiles") version "4.12.0"
}

repositories {
    gradlePluginPortal()
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("com.yworks.yfiles:yfiles-kotlin:23.0.3-SNAPSHOT")
}

tasks.wrapper {
    gradleVersion = "6.8-rc-1"
    distributionType = Wrapper.DistributionType.ALL
}
