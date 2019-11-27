plugins {
    kotlin("jvm") version "1.3.61"
}

repositories {
    jcenter()
}

kotlin {
    sourceSets {
        main {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("nu.studer:java-ordered-properties:1.0.2")
            }
        }
    }
}
