import io.github.turansky.yfiles.generateKotlinWrappers

group = "com.yworks.yfiles"
version = "2.2.0-SNAPSHOT"

plugins {
    id("kotlin2js")
    id("maven-publish")
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

tasks {
    clean {
        doLast {
            delete("out")
        }
    }

    compileKotlin2Js {
        kotlinOptions {
            outputFile = "$projectDir/out/yfiles-kotlin.js"
            moduleKind = "amd"
            metaInfo = true
        }

        doFirst {
            val apiPath = "http://docs.yworks.com/yfileshtml/assets/api.8ff904af.js"
            generateKotlinWrappers(apiPath, File(projectDir, "src/main/kotlin"))
        }
    }
}

/*
task mainJar(type: Jar) {
    from "$projectDir/out"
}

task sourceJar(type: Jar) {
    from "$projectDir/src/main/kotlin"
}

publishing {
    publications {
        maven(MavenPublication) {
            artifact mainJar

            artifact sourceJar {
                classifier "sources"
            }
        }
    }
}
*/