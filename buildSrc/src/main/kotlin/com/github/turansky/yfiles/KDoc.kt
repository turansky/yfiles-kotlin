package com.github.turansky.yfiles

const val LIST_MARKER = "-"
const val LINE_DELIMITER = "\n"
const val MULTILINE_INDENT = "  "

// TODO: remove after fix
//  https://youtrack.jetbrains.com/issue/KT-32815
private fun String.applyAnchorWorkaround(): String =
    if ("#" in this) {
        val index = indexOf("#") + 1
        substring(0, index) + substring(index).replace("#", "%23")
    } else {
        this
    }

// TODO: use Markdown after fix
//  https://youtrack.jetbrains.com/issue/KT-32640
fun link(text: String, href: String): String =
    """<a href="${href.applyAnchorWorkaround()}">$text</a>"""

fun listItem(text: String): String =
    "$LIST_MARKER $text"

fun constructor(): String =
    "@constructor"

fun constructor(summary: String): String =
    "@constructor $summary"

fun property(name: String): String =
    "@property $name"

fun param(name: String, summary: String): List<String> =
    "@param [$name] $summary"
        .asMultiline()

fun ret(summary: String): List<String> =
    "@return $summary"
        .asMultiline()

fun throws(summary: String): String =
    "@throws $summary"

fun see(link: String): String =
    "@see $link"

private fun String.asMultiline(): List<String> =
    split(LINE_DELIMITER)
        // TODO: check if required
        .filter { it.isNotEmpty() }
        .mapIndexed { index, line ->
            if (index == 0) {
                line
            } else {
                MULTILINE_INDENT + line
            }
        }
