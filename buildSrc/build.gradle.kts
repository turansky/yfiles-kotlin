plugins {
    kotlin("jvm") version "1.3.70"
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

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
            allWarningsAsErrors = true
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.json", "json", "20190722")
}
