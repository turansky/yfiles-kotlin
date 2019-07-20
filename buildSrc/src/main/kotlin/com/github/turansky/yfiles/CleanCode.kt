package com.github.turansky.yfiles

internal fun String.clear(data: GeneratorData): String {
    var content = replace(data.packageName + ".", "")
        .replace(Regex("(\\n\\s?){3,}"), "\n\n")
        .replace(Regex("(\\n\\s?){2,}}"), "\n}")

    val regex = Regex("yfiles\\.([a-z]+)\\.([A-Za-z0-9]+)")

    val code = content
        .split("\n")
        .asSequence()
        .filterNot { it.startsWith(" *") }
        .joinToString("\n")

    val importedClasses = regex
        .findAll(code)
        .map { it.value }
        .distinct()
        // TODO: remove after es6name use
        // WA for duplicated class names (Insets for example)
        .filterNot { it.endsWith("." + data.name) }
        .plus(
            STANDARD_IMPORTED_TYPES
                .asSequence()
                .filter { code.contains(it) }
        )
        .sorted()
        .toList()

    if (importedClasses.isEmpty()) {
        return content
    }

    val imports = importedClasses
        .lines { "import $it" }

    for (className in importedClasses) {
        val name = className.substringAfterLast(".")
        content = content.replace(className, name)
    }

    return "$imports\n$content"
}
