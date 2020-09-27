plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
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
