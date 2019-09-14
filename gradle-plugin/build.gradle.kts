group = "com.github.turansky.yfiles"
version = "0.0.1-SNAPSHOT"

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.3.50"
}

repositories {
    jcenter()
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
    compileOnly(kotlin("compiler-embeddable"))
}

gradlePlugin {
    plugins {
        create("yfiles") {
            id = "com.github.turansky.yfiles"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.GradlePlugin"
        }
    }
}