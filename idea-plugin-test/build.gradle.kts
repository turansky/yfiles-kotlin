plugins {
    kotlin("js") version "1.3.60"
}

allprojects {
    repositories {
        jcenter()
    }
}

kotlin {
    target {
        nodejs()
    }

    sourceSets {
        main {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

tasks {
    compileKotlinJs {
        kotlinOptions {
            moduleKind = "commonjs"
            allWarningsAsErrors = true
        }
    }

    wrapper {
        gradleVersion = "6.0.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}
