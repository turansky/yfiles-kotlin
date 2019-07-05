plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation(kotlin("stdlib"))

    compileOnly("com.google.auto.service", "auto-service", "1.0-rc5")
}

gradlePlugin {
    plugins {
        create("com.github.turansky.yfiles") {
            id = "com.github.turansky.yfiles"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.YPlugin"
        }
    }
}