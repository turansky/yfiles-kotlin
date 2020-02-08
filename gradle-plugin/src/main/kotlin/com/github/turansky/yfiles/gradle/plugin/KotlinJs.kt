package com.github.turansky.yfiles.gradle.plugin

internal object KotlinJs {
    val GRADLE_PLUGIN_ID = "org.jetbrains.kotlin.js"

    val COMPILE_TASK_NAME = "compileKotlinJs"
    val COMPILE_TEST_TASK_NAME = "compileTestKotlinJs"
    val COMPILE_TASK_NAMES = setOf(
        COMPILE_TASK_NAME,
        COMPILE_TEST_TASK_NAME
    )

    val TARGET_V5 = "v5"

    val OUTPUT_FILE = "outputFile"
}
