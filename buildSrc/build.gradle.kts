plugins {
    kotlin("jvm") version "1.3.31"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.json", "json", "20180813")
}