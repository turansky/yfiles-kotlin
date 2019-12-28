plugins {
    kotlin("jvm") version "1.3.61"
    id("com.github.autostyle") version "3.0"
}

repositories {
    jcenter()
}

autostyle {
    kotlin {
        endWithNewline()
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("nu.studer:java-ordered-properties:1.0.2")
}
