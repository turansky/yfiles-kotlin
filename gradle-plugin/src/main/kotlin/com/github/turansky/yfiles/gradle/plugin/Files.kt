package com.github.turansky.yfiles.gradle.plugin

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.streams.toList

internal fun copyDirectory(
    sourceDir: File,
    targetDir: File
) {
    val source: Path = sourceDir.toPath()
    val destination: Path = targetDir.toPath()
    val sources: List<Path> = Files.walk(source).toList()

    sources.asSequence()
        .map(source::relativize)
        .map(destination::resolve)
        .forEachIndexed { index, destinationPath ->
            Files.copy(sources[index], destinationPath, StandardCopyOption.REPLACE_EXISTING)
        }
}
