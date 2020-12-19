plugins {
    kotlin("js") version "1.4.21"
    id("com.github.turansky.kfc.library") version "2.1.0"
    id("com.github.turansky.yfiles") version "5.4.0"
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
    gradleVersion = "6.8-rc-3"
    distributionType = Wrapper.DistributionType.ALL
}
