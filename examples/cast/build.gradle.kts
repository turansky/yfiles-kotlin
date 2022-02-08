plugins {
    id("io.github.turansky.kfc.library")
    id("com.github.turansky.yfiles")
}

kotlin.js {
    binaries.executable()
}

dependencies {
    implementation(project(":yfiles-kotlin"))
}
