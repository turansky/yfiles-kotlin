package com.github.turansky.yfiles.correction

internal fun applyExecutorHacks(source: Source) {
    source.type("LayoutExecutorAsync") {
        method("createWebWorkerMessageHandler").firstParameter[TYPE] = "web.workers.Worker"
    }
}