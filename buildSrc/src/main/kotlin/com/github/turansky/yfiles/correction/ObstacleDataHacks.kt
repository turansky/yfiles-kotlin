package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

private const val OBSTACLE_DATA = "yfiles.router.ObstacleData"

internal fun generateObstacleData(context: GeneratorContext) {
    // language=kotlin
    context[OBSTACLE_DATA] =
        """
            |package yfiles.router
            |
            |external interface ObstacleData
        """.trimMargin()
}

internal fun applyObstacleDataHacks(source: Source) {
    source.type("Obstacle").apply {
        flatMap(CONSTRUCTORS)
            .flatMap(PARAMETERS)
            .filter { it[NAME] == "data" }
            .plus(property("data"))
            .forEach { it[TYPE] = OBSTACLE_DATA }
    }

    source.type("GraphPartition")
        .flatMap(METHODS)
        .filter { it.has(RETURNS) }
        .filter { it[RETURNS][TYPE] == "yfiles.router.Obstacle" }
        .flatMap(PARAMETERS)
        .filter { it[NAME] == "data" }
        .forEach { it[TYPE] = OBSTACLE_DATA }
}
