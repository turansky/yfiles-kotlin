plugins {
    kotlin("js") version "1.3.71"
    id("com.github.turansky.yfiles") version "0.13.0"
}

repositories {
    gradlePluginPortal()
    jcenter()
    mavenLocal()
}

kotlin {
    target {
        nodejs()
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("com.yworks.yfiles:yfiles-kotlin:22.0.3-SNAPSHOT")
}

tasks {
    compileKotlinJs {
        kotlinOptions {
            moduleKind = "commonjs"
            allWarningsAsErrors = true
        }
    }

    wrapper {
        gradleVersion = "6.2.2"
        distributionType = Wrapper.DistributionType.ALL
    }
}
