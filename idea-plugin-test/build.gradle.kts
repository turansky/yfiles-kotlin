plugins {
    kotlin("js") version "1.3.72"
    id("com.github.turansky.yfiles") version "0.13.0"
}

repositories {
    gradlePluginPortal()
    jcenter()
    mavenLocal()
}

kotlin.js {
    nodejs()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("com.yworks.yfiles:yfiles-kotlin:23.0.2-SNAPSHOT")
}

tasks {
    compileKotlinJs {
        kotlinOptions {
            moduleKind = "commonjs"
            allWarningsAsErrors = true
        }
    }

    wrapper {
        gradleVersion = "6.6"
        distributionType = Wrapper.DistributionType.ALL
    }
}
