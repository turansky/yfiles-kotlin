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

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.json", "json", "20180813")
}
