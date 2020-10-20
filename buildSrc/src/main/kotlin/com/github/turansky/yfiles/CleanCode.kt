package com.github.turansky.yfiles

private val LINE_BREAK_3 = Regex("(\\n\\s?){3,}")
private val LINE_BREAK_2 = Regex("(\\n\\s?){2,}}")

private val YFILES_CLASS_DECLARATION = Regex("yfiles\\.([a-z]+)\\.([A-Za-z0-9]+)")
private val DUPLICATED_LINK = Regex("(\\[[a-zA-Z0-9.]+])\\1")
private val LONG_LINK = Regex("([^]])\\[(yfiles\\.[a-z]+)\\.([^]]+)]")

internal fun String.clear(fqn: String): String {
    val packageName = fqn.substringBeforeLast(".")
    var content = replace("[${fqn}.", "[")
        .replace("$packageName.", "")
        .replace(LINE_BREAK_3, "\n\n")
        .replace(LINE_BREAK_2, "\n}")

    if (!content.endsWith("\n")) {
        content += "\n"
    }

    val importedClasses = content.getImportedClasses()

    if (importedClasses.isEmpty()) {
        return content
    }

    val imports = importedClasses
        .lines { "import $it" }

    for (className in importedClasses) {
        val name = className.substringAfterLast(".")
        content = content.replace(className, name)
    }

    content = content.cleanDoc()

    return "$imports\n$content"
}

private fun String.cleanDoc(): String {
    return replace(DUPLICATED_LINK, "$1")
        .replace(LONG_LINK, "$1[$3][$2.$3]")
}

private fun String.getImportedClasses(): List<String> {
    val code = split("\n")
        .asSequence()
        .filterNot { it.startsWith("import yfiles.") }
        .filterNot { it.startsWith(" *") }
        .joinToString("\n")

    val additionalImports = if ("js.yclass" in this)
        listOf("yfiles.lang.yclass")
    else
        emptyList()

    return YFILES_CLASS_DECLARATION
        .findAll(code)
        .map { it.value }
        .plus(additionalImports)
        .distinct()
        .plus(
            STANDARD_IMPORTED_TYPES
                .asSequence()
                .filter { it in code }
        )
        .sorted()
        .toList()
}
