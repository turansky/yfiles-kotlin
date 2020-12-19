plugins {
    kotlin("js") apply false
}

allprojects {
    repositories {
        jcenter()
    }
}

tasks.wrapper {
    gradleVersion = "6.8-rc-3"
    distributionType = Wrapper.DistributionType.ALL
}
