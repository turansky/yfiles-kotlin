group = "com.github.turansky.yfiles"
version = "0.0.1-SNAPSHOT"

repositories {
    jcenter()
}

tasks.wrapper {
    gradleVersion = "6.0.1"
    distributionType = Wrapper.DistributionType.ALL
}