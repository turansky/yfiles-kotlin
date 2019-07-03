plugins {
    kotlin("jvm") version "1.3.41"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.json", "json", "20180813")
}