plugins {
    kotlin("js")
}

tasks {
    compileKotlinJs {
        kotlinOptions {
            moduleKind = "amd"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":yfiles-kotlin"))
}