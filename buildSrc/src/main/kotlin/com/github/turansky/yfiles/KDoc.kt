package com.github.turansky.yfiles

// TODO: use Markdown after fix
//  https://youtrack.jetbrains.com/issue/KT-32640
fun link(text: String, href: String): String =
    """<a href="$href">$text</a>"""

fun listItem(text: String): String =
    "- $text"

fun constructor(): String =
    "@constructor"

fun constructor(summary: String): String =
    "@constructor $summary"

fun param(name: String, summary: String): String =
    "@param [$name] $summary"

fun ret(summary: String): String =
    "@return $summary"

fun throws(summary: String): String =
    "@throws $summary"

fun see(link: String): String =
    "@see $link"
