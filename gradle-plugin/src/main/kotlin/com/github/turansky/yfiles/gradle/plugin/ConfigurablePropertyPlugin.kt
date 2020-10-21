// TODO: Remove after fix https://youtrack.jetbrains.com/issue/KT-42364

package com.github.turansky.yfiles.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

private object KotlinJs {
    val PLUGIN_ID = "org.jetbrains.kotlin.js"

    val COMPILE_TASK_NAMES = setOf(
        "compileKotlinJs",
        "compileTestKotlinJs"
    )

    val TARGET_V5 = "v5"
}

private val DESCRIPTOR_REGEX = Regex("(Object\\.defineProperty\\(.+\\.prototype, '[a-zA-Z]+', \\{)(\\n\\s+get:)")

internal class ConfigurablePropertyPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins.withId(KotlinJs.PLUGIN_ID) {
            tasks.withType<KotlinJsCompile>().configureEach {
                doLast {
                    val jsTarget = kotlinOptions.target
                    if (jsTarget != KotlinJs.TARGET_V5) {
                        logger.warn("Unsupported JS target '$jsTarget'. Fix for KT-42364 won't be applied!")
                        return@doLast
                    }

                    if (name !in KotlinJs.COMPILE_TASK_NAMES)
                        return@doLast

                    val outputFile = file(kotlinOptions.outputFile!!)
                    // IR invalid folder check
                    if (outputFile.parentFile.name != "kotlin")
                        return@doLast

                    val content = outputFile.readText()
                    val newContent = content
                        .replace(DESCRIPTOR_REGEX, "$1\n    configurable: true,$2")

                    if (newContent != content) {
                        outputFile.writeText(newContent)
                    }
                }
            }
        }
    }
}
