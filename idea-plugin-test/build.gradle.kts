plugins {
    kotlin("js") version "1.4.10"
    id("com.github.turansky.kfc.library") version "1.0.0"
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
    gradleVersion = "6.7"
    distributionType = Wrapper.DistributionType.ALL
}
