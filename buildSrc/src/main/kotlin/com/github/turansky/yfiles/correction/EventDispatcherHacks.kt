package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IEVENT_DISPATCHER
import java.io.File

internal fun generateEventDispatcherUtils(sourceDir: File) {
    sourceDir.resolve("yfiles/lang/IEventDispatcher.kt")
        .writeText(
            // language=kotlin
            """
                |package yfiles.lang
                |
                |external interface IEventDispatcher
            """.trimMargin()
        )
}

internal fun applyEventDispatcherHacks(source: Source) {
    source.types()
        .filter { it.has(EVENTS) }
        .forEach {
            if (it.has(IMPLEMENTS)) {
                it[IMPLEMENTS].put(IEVENT_DISPATCHER)
            } else {
                it[IMPLEMENTS] = arrayOf(IEVENT_DISPATCHER)
            }
        }
}
