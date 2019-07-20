package com.github.turansky.yfiles

private val LINE_BREAK_3 = Regex("(\\n\\s?){3,}")
private val LINE_BREAK_2 = Regex("(\\n\\s?){2,}}")

private val YFILES_CLASS_DECLARATION = Regex("yfiles\\.([a-z]+)\\.([A-Za-z0-9]+)")
private val DUPLICATED_LINK = Regex("(\\[[a-zA-Z0-9.]+])\\1")

internal fun String.clear(data: GeneratorData): String {
    var content = replace(data.packageName + ".", "")
        .replace(LINE_BREAK_3, "\n\n")
        .replace(LINE_BREAK_2, "\n}")

    val code = content
        .split("\n")
        .asSequence()
        .filterNot { it.startsWith(" *") }
        .joinToString("\n")

    val importedClasses = YFILES_CLASS_DECLARATION
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

    content = content.replace(DUPLICATED_LINK, "$1")

    return "$imports\n$content"
}
