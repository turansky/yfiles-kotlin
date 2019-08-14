plugins {
    kotlin("jvm") version "1.3.50-eap-86"
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
    maven(url = "https://kotlin.bintray.com/kotlin-eap")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.json", "json", "20180813")
}