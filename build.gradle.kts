plugins {
    kotlin("js") apply false
}

allprojects {
    repositories {
        jcenter()
    }
}

tasks.wrapper {
    gradleVersion = "6.8-rc-4"
    distributionType = Wrapper.DistributionType.ALL
}
