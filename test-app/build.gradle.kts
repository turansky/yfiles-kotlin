plugins {
    id("kotlin2js")
}

tasks {
    compileKotlin2Js {
        kotlinOptions {
            moduleKind = "amd"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":yfiles-kotlin"))
}