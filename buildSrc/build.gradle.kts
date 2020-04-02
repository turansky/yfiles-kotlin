plugins {
    kotlin("jvm") version "1.3.71"
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
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.json", "json", "20190722")
}
