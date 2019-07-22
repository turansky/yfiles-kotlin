plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.3.41"
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
}

gradlePlugin {
    plugins {
        create("com.github.turansky.yfiles") {
            id = "com.github.turansky.yfiles"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.GradlePlugin"
        }
    }
}