plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

tasks.compileKotlin {
    kotlinOptions.allWarningsAsErrors = true
}

dependencies {
    implementation("org.json:json:20200518")
}
