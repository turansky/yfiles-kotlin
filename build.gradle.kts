plugins {
    kotlin("js") version "1.3.40" apply false
}

allprojects {
    repositories {
        jcenter()
    }
}

tasks.wrapper {
    gradleVersion = "5.5"
    distributionType = Wrapper.DistributionType.ALL
}