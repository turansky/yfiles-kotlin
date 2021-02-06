plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

tasks.compileKotlin {
    kotlinOptions.allWarningsAsErrors = true
}

dependencies {
    implementation("org.json:json:20200518")
}
