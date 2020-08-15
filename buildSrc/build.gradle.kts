plugins {
    kotlin("jvm") version "1.3.72"
}

repositories {
    jcenter()
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
        allWarningsAsErrors = true
    }
}

dependencies {
    implementation("org.json:json:20200518")
}
