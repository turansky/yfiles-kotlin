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
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
}

gradlePlugin {
    plugins {
        create("com.github.turansky.yfiles") {
            id = "com.github.turansky.yfiles"
            implementationClass = "com.github.turansky.yfiles.gradle.plugin.YPlugin"
        }
    }
}