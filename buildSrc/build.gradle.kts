plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

tasks.compileKotlin {
    kotlinOptions.allWarningsAsErrors = true
}

dependencies {
    implementation("org.json:json:20200518")
}
