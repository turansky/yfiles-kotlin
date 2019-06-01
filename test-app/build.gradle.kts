import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("kotlin2js")
}

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "amd"
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":api"))
}