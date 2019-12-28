plugins {
    kotlin("jvm") version "1.3.61"
    id("com.github.autostyle") version "3.0"
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

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.json", "json", "20180813")
}
